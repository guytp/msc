package org.guytp.mscexperiment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class IntroPanasActivity extends KioskActivity {
    private Button _nextButton;
    private String[] _emotionWords;
    private Button[] _answerButtons;
    private TextView _emotionWordLabel;
    private String _selectedValue;
    private int _currentEmotionWordIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_panas);

        // Setup emotion words
        _currentEmotionWordIndex = 0;
        _emotionWords = new String[]{ "Interested", "Distressed", "Excited", "Upset", "Strong", "Guilty", "Scared", "Hostile", "Enthusiastic", "Proud", "Irritable", "Alert", "Ashamed", "Inspired", "Nervous", "Determined", "Attentive", "Jittery", "Active", "Afraid" };

        // Get a handle to controls
        _nextButton = (Button)findViewById(R.id.nextButton);
        _emotionWordLabel = (TextView)findViewById(R.id.emotionWordLabel);
        _emotionWordLabel.setText(_emotionWords[0]);
        _answerButtons = new Button[5];
        _answerButtons[0] = (Button)findViewById(R.id.answerButton1);
        _answerButtons[1] = (Button)findViewById(R.id.answerButton2);
        _answerButtons[2] = (Button)findViewById(R.id.answerButton3);
        _answerButtons[3] = (Button)findViewById(R.id.answerButton4);
        _answerButtons[4] = (Button)findViewById(R.id.answerButton5);
        resetButtonStates();

        // Signify start of first question
        ExperimentData.getInstance().addTimeMarker("IntroPanas-" + _emotionWords[_currentEmotionWordIndex], "Show");
    }

    public void onNextPress(View v) {
        // Store data from last view and ensure valid data entry
        if (_selectedValue == null || _selectedValue.length() < 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("You must select an answer")
                    .setNeutralButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }
        ExperimentData.getInstance().addData("Panas." + _emotionWords[_currentEmotionWordIndex], _selectedValue);

        // Mark this question completed for timing
        ExperimentData.getInstance().addTimeMarker("IntroPanas-" + _emotionWords[_currentEmotionWordIndex], "Finish");

        // Move to next screen if we're done with questions
        if (_currentEmotionWordIndex == _emotionWords.length - 1)
        {
            startActivity(new Intent(IntroPanasActivity.this, Phase1IntroductionActivity.class));
            return;
        }

        // Otherwise show the next question and start a new timer
        _currentEmotionWordIndex++;
        _emotionWordLabel.setText(_emotionWords[_currentEmotionWordIndex]);
        _nextButton.setEnabled(false);
        resetButtonStates();
        ExperimentData.getInstance().addTimeMarker("IntroPanas-" + _emotionWords[_currentEmotionWordIndex], "Show");
    }

    public void onAnswerButtonPress(View v) {
        Button b = (Button)v;
        _selectedValue = b.getText().toString();
        for (int i = 0; i < _answerButtons.length; i++) {
            Button btn = _answerButtons[i];
            btn.setBackgroundColor(btn == b ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        }
        _nextButton.setEnabled(true);
    }

    private void resetButtonStates() {
        for (int i = 0; i < _answerButtons.length; i++) {
            Button btn = _answerButtons[i];
            btn.setBackgroundColor(Color.rgb(171, 180, 186));
            btn.setTextColor(Color.BLACK);
        }
    }
}
