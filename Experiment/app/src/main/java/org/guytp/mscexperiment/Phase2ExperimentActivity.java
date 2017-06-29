package org.guytp.mscexperiment;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Phase2ExperimentActivity extends KioskActivity {

    private final double _stateDuration = 20;

    private final int _maximumStates = 20;


    private CushionController _cushionController;

    private CushionState[] _cushionStates;

    private int _nextStateToShow = 0;

    private SeekBar _sliderEnergy;

    private SeekBar _sliderPleasantness;

    private Boolean _isEnergyTouched = false;

    private Boolean _isPleasantnessTouched = false;

    private View _answerLayout;

    private View _stateOnLayout;

    private Handler _timer;

    private Runnable _displayStateRunnable;

    private Runnable _turnOffRunnable;

    private TextView _questionLabel;

    private TextView _stateOnLabel;

    private Button _nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup basics
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase2_experiment);

        // Hookup to controls
        _sliderEnergy = (SeekBar)findViewById(R.id.sliderEnergy);
        _sliderPleasantness = (SeekBar)findViewById(R.id.sliderPleasantness);
        _answerLayout = findViewById(R.id.answerLayout);
        _stateOnLayout = findViewById(R.id.stateOnLayout);
        _stateOnLabel = (TextView)findViewById(R.id.stateOnLabel);
        _questionLabel = (TextView)findViewById(R.id.questionLabel);
        _nextButton = (Button)findViewById(R.id.nextButton);

        // Hookup to slider events
        final Button nextButton = _nextButton;
        SeekBar.OnSeekBarChangeListener seekBarChangedListener = new SeekBar.OnSeekBarChangeListener(){
            public void onStopTrackingTouch(SeekBar seekBar) {}

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (seekBar == _sliderEnergy)
                    _isEnergyTouched = true;
                else
                    _isPleasantnessTouched = true;
                nextButton.setEnabled(_isEnergyTouched && _isPleasantnessTouched);
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){}
        };
        _sliderEnergy.setOnSeekBarChangeListener(seekBarChangedListener);
        _sliderPleasantness.setOnSeekBarChangeListener(seekBarChangedListener);

        // Determine which states we will show
        _cushionStates = CushionState.randomlyOrderedStatesSets(5);
        if (_cushionStates.length > _maximumStates && _maximumStates > 0) {
            CushionState[] newStates = new CushionState[_maximumStates];
            for (int i = 0; i < _maximumStates; i++)
                newStates[i] = _cushionStates[i];
            _cushionStates = newStates;
        }
        for (int i = 0; i < _cushionStates.length; i++)
            ExperimentData.getInstance().addData("Phase2Experiment.State" + (i + 1), _cushionStates[i].toString());
        _cushionController = CushionController.getInstance();

        // Hide answering UI and show first question
        _answerLayout.setVisibility(View.GONE);
        _stateOnLayout.setVisibility(View.VISIBLE);

        // Store the runnables used for setting a state and turning off
        _displayStateRunnable = new Runnable() { @Override public void run() { displayStateRunnable(); } };
        _turnOffRunnable = new Runnable() { @Override public void run() { turnOffRunnable(); } };

        // Schedule execution very soon of first state
        _timer = new Handler();
        _timer.postDelayed(_displayStateRunnable, 100);

        // Record the start of this screen
        ExperimentData.getInstance().addTimeMarker("Phase2Experiment", "Show");
    }

    private void displayStateRunnable() {
        // Set valid state
        _cushionController.setState(_cushionStates[_nextStateToShow]);

        // Display in UI
        _stateOnLabel.setText("This is the " + Ordinals.values[_nextStateToShow] + " state.");
        ExperimentData.getInstance().addTimeMarker("Phase2Experiment.StateShow" + (_nextStateToShow + 1), "On");

        // Scheduled off in 10 seconds
        _timer.postDelayed(_turnOffRunnable, (int)(_stateDuration * 1000));
    }

    private void turnOffRunnable() {
        // Turn off
        _cushionController.off();
        ExperimentData.getInstance().addTimeMarker("Phase2Experiment.StateShow" + (_nextStateToShow + 1), "Off");

        // Increment next state to show
        _nextStateToShow++;

        // Transition to answer phase
        ExperimentData.getInstance().addTimeMarker("Phase2Experiment.StateQuestion" + (_nextStateToShow - 1) + "-" + (_nextStateToShow), "Show");
        _questionLabel.setText("You just saw the " + Ordinals.values[_nextStateToShow - 1] + " state.  Please move the circles below to indicate how much energy you thought the state had and how pleasant it was.  Once you're done press the Next button.");
        _answerLayout.setVisibility(View.VISIBLE);
        _stateOnLayout.setVisibility(View.GONE);
        _nextButton.setEnabled(false);
    }

    public void onNextPress(View v) {
        // Record the participant's answer
        ExperimentData.getInstance().addData("Phase2Experiment.State" + (_nextStateToShow) + ".State", _cushionStates[_nextStateToShow - 1].toString());
        ExperimentData.getInstance().addData("Phase2Experiment.State" + (_nextStateToShow) + ".Energy", Integer.toString(_sliderEnergy.getProgress() - 100));
        ExperimentData.getInstance().addData("Phase2Experiment.State" + (_nextStateToShow) + ".Pleasantness", Integer.toString(_sliderPleasantness.getProgress() - 100));

        // If this is the last state move on to complete activity
        if (_nextStateToShow == _cushionStates.length) {
            ExperimentData.getInstance().addTimeMarker("Phase2Experiment", "Finish");
            startActivity(new Intent(Phase2ExperimentActivity.this, Phase2CompleteActivity.class));
            return;
        }

        // Hide the answer UI and start the next state along with a timer to turn it off
        _answerLayout.setVisibility(View.GONE);
        _stateOnLayout.setVisibility(View.VISIBLE);
        displayStateRunnable();
        _isEnergyTouched = false;
        _isPleasantnessTouched = false;
        _sliderPleasantness.setProgress(100);
        _sliderEnergy.setProgress(100);
    }
}
