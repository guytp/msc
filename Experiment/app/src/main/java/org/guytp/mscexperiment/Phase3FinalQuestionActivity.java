package org.guytp.mscexperiment;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class Phase3FinalQuestionActivity extends KioskActivity {

    private String _selection;
    private Button _nextButton;
    private Button[] _buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_final_question);
        _nextButton = (Button)findViewById(R.id.nextButton);
        _nextButton.setEnabled(false);
        _buttons = new Button[3];
        Button answerButton1 = (Button)findViewById(R.id.answerButton1);
        Button answerButton2 = (Button)findViewById(R.id.answerButton2);
        Button answerButton3 = (Button)findViewById(R.id.answerButton3);
        _buttons[0] = answerButton1;
        _buttons[1] = answerButton2;
        _buttons[2] = answerButton3;
        if (new Random().nextDouble() > 0.5) {
            answerButton1.setText("Vibration");
            answerButton2.setText("Light");
        } else {
            answerButton1.setText("Light");
            answerButton2.setText("Vibration");
        }
        ExperimentData.getInstance(this).addTimeMarker("Phase3FinalQuestion", "Show");
    }

    public void onAnswerButtonPress(View v) {
        _selection = ((Button)v).getText().toString();
        ExperimentData.getInstance(this).addTimeMarker("Phase3FinalQuestion", "Press." + _selection);
        Button b = (Button)v;
        for (int i = 0; i < _buttons.length; i++) {
            Button btn = _buttons[i];
            btn.setBackgroundColor(btn == b ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        }
        _nextButton.setEnabled(true);
    }

    public void onNextPress(View v) {
        ExperimentData.getInstance(this).addData("Phase3.VibrationOrLight", _selection);
        ExperimentData.getInstance(this).addTimeMarker("Phase3FinalQuestion", "Finish");
        startActivity(new Intent(Phase3FinalQuestionActivity.this, OutroPanasActivity.class));
    }
}