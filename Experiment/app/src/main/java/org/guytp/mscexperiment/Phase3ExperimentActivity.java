package org.guytp.mscexperiment;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class Phase3ExperimentActivity extends KioskActivity {

    private String[] _emotionWords = {
            "Excited", "Happy", "Surprised", "Delighted", "Cheerful", // High Energy, Pleasant
            "Relaxed", "Calm", "Serene", "Content", "Pleased",  // Low Energy, Pleasant
            "Sad", "Depressed", "Gloomy", "Bored", "Tired", // Low Energy, Unpleasant
            "Afraid", "Angry", "Annoyed", "Frustrated", "Terrified" // High Energy, Unpleasant
            };
            // Mix of Russell and Schubert : https://www.researchgate.net/figure/228583824_fig10_Figure-10-Ratings-of-emotion-words-in-a-two-dimensional-emotion-space-according-to

    private Button[] _playStateButtons;
    private Button[] _stateButtons;
    private Button _nextButton;
    private TextView _emotionLabel;
    private TextView _instructionLabel;

    private int _nextWord = 0;
    private int _selectionsForWord;
    private CushionState[] _cushionStates;
    private CushionState _selectedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hookup to UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_experiment);

        // Attach to views from the UI
        _stateButtons = new Button[5];
        _stateButtons[0] = (Button)findViewById(R.id.stateButton1);
        _stateButtons[1] = (Button)findViewById(R.id.stateButton2);
        _stateButtons[2] = (Button)findViewById(R.id.stateButton3);
        _stateButtons[3] = (Button)findViewById(R.id.stateButton4);
        _stateButtons[4] = (Button)findViewById(R.id.stateButton5);
        _playStateButtons = new Button[4];
        _playStateButtons[0] = (Button)findViewById(R.id.playStateButton1);
        _playStateButtons[1] = (Button)findViewById(R.id.playStateButton2);
        _playStateButtons[2] = (Button)findViewById(R.id.playStateButton3);
        _playStateButtons[3] = (Button)findViewById(R.id.playStateButton4);
        _nextButton = (Button)findViewById(R.id.nextButton);
        _emotionLabel = (TextView)findViewById(R.id.emotionWordLabel);
        _instructionLabel = (TextView)findViewById(R.id.instructionLabel);

        // Generate our cushion states in a random order and log them
        CushionState[] cushionStates = CushionState.randomlyOrderedStates();
        _cushionStates = new CushionState[cushionStates.length + 1];
        for (int i = 0; i < cushionStates.length; i++) {
            ExperimentData.getInstance(this).addData("Phase3Experiment.State" + (i + 1), cushionStates[i].toString());
            _cushionStates[i] = cushionStates[i];
        }
        _cushionStates[cushionStates.length] = CushionState.None;

        // Randomise our emotional words with a shuffle
        Random random = new Random();
        for (int i = _emotionWords.length - 1; i > 0; i--)
        {
            int index = random.nextInt(i + 1);
            String word = _emotionWords[index];
            _emotionWords[index] = _emotionWords[i];
            _emotionWords[i] = word;
        }

        // Log our word order in the data set
        for (int i = 0; i < _emotionWords.length; i++)
            ExperimentData.getInstance(this).addData("Phase3Experiment.Word" + (i + 1), _emotionWords[i]);

        // Setup for next word
        setupNextWord();

        // Setup that phase 3 experiment has started
        ExperimentData.getInstance(this).addTimeMarker("Phase3Experiment", "Show");
    }

    private void setupNextWord() {
        // Update text of word
        _emotionLabel.setText(_emotionWords[_nextWord]);
        _instructionLabel.setText("Play each of the four states and select the one that best represents (" + (_nextWord + 1) + " of " + _emotionWords.length + "):");

        // Reset button states
        setActiveStateButton(null, _stateButtons);
        setActiveStateButton(null, _playStateButtons);
        _nextButton.setEnabled(false);

        // Turn off cushion
        CushionController.getInstance(this).off();

        // Update counters
        _nextWord++;
        _selectionsForWord = 0;

        // Log this
        ExperimentData.getInstance(this).addTimeMarker("Phase3Experiment", "Word" + _nextWord + ".Show");
    }

    public void onPlayStateButtonPress(View v) {
        // Get a handle to this button
        Button b = (Button)v;
        int buttonNumber = b == _playStateButtons[0] ? 1 : b == _playStateButtons[1] ? 2 : b == _playStateButtons[2] ? 3 : 4;
        CushionState state = _cushionStates[buttonNumber - 1];
        if (b.getText() == "▶") {
            setActiveStateButton(b, _playStateButtons);
            CushionController.getInstance(this).setState(state);
            ExperimentData.getInstance(this).addTimeMarker("Phase3Experiment.Word" + _nextWord + ".PlayStateStart", state.toString());
        } else {
            setActiveStateButton(null, _playStateButtons);
            CushionController.getInstance(this).off();
            ExperimentData.getInstance(this).addTimeMarker("Phase3Experiment.Word" + _nextWord + ".PlayStateStop", state.toString());
        }
    }

    public void onStateButtonPress(View v) {
        // Determine which button was pressed
        Button b = (Button)v;
        int buttonNumber = b.getText().toString().equals("None") ? 5 : Integer.parseInt(b.getText().toString());
        CushionState state = _cushionStates[buttonNumber - 1];

        // Update the state of all buttons
        setActiveStateButton(b, _stateButtons);
        _nextButton.setEnabled(true);

        // Record which one has been selected and increment selection count
        _selectionsForWord++;
        _selectedState = state;

        // Log which one has been selected
        ExperimentData.getInstance(this).addData("Phase3Experiment.Word" + _nextWord + ".Selection" + _selectionsForWord, state.toString());

        // If this is calm or angry that has been displayed as a word then store the associated state - this is used by the final part of the
        // experiment to activate one of the two states selected for these words
        if (_emotionWords[_nextWord - 1].equals("Angry"))
            ExperimentData.getInstance(this).addData("Phase3Experiment.AngryState", (state == CushionState.None ? CushionState.Angry : state).toString());
        else if (_emotionWords[_nextWord - 1].equals("Calm"))
            ExperimentData.getInstance(this).addData("Phase3Experiment.CalmState", (state == CushionState.None ? CushionState.Calm : state).toString());
    }

    public void onNextPress(View v) {
        // Record the selected state
        ExperimentData.getInstance(this).addTimeMarker("Phase3Experiment", "Word" + _nextWord + ".Finish");
        ExperimentData.getInstance(this).addData("Phase3Experiment.Word" + _nextWord + ".Selection", _selectedState.toString());
        CushionController.getInstance(this).off();

        // If we're at end then transition to Phase3
        if (_nextWord == _emotionWords.length) {
            ExperimentData.getInstance(this).addTimeMarker("Phase3Experiment", "Finish");
            startActivity(new Intent(Phase3ExperimentActivity.this, Phase3HoldCushionActivity.class));
            return;
        }

        // Show the next word
        setupNextWord();
    }

    private void setActiveStateButton(Button button, Button[] btns) {
        for (int i = 0; i < btns.length; i++) {
            btns[i].setBackgroundColor(btns[i] == button ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
            if (btns == _playStateButtons)
                btns[i].setText(btns[i] == button ? "■" : "▶");
        }
    }
}
