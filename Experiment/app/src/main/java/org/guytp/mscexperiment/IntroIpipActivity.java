package org.guytp.mscexperiment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class IntroIpipActivity extends KioskActivity {

    private String[] _questions = {
            "Have frequent mood swings",
            "Am relaxed most of the time",
            "Get upset easily",
            "Seldom feel blue"
    };

    private int _score = 0;

    private int _totalScore = 0;

    private boolean[] _isScoringReverse = { false, true, false, true  };

    private Button[] _buttons = new Button[5];

    private Button _nextButton;

    private TextView _label;

    private int _currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_ipip);

        // Get a handle to UI
        _buttons[0] = (Button)findViewById(R.id.answerButton1);
        _buttons[1] = (Button)findViewById(R.id.answerButton2);
        _buttons[2] = (Button)findViewById(R.id.answerButton3);
        _buttons[3] = (Button)findViewById(R.id.answerButton4);
        _buttons[4] = (Button)findViewById(R.id.answerButton5);
        _nextButton = (Button)findViewById(R.id.nextButton);
        _label = (TextView)findViewById(R.id.statementLabel);

        // Setup initial state
        _nextButton.setEnabled(false);
        _label.setText(_questions[_currentQuestionIndex]);

        // Log that we're here
        ExperimentData.getInstance(this).addTimeMarker("IntroIpip", "Show");
    }

    public void onNextPress(View v) {
        // Store value
        ExperimentData.getInstance(this).addData("IntroIpip.Question" + (_currentQuestionIndex + 1) + ".Score", Integer.toString(_score));
        _totalScore += _score;

        // If we're at the end, transition to next screen
        if (_currentQuestionIndex == _questions.length - 1) {
            ExperimentData.getInstance(this).addData("IntroIpip.FinalScore", Double.toString((double)_totalScore / (double)_questions.length));
            ExperimentData.getInstance(this).addTimeMarker("IntroIpip", "Finish");
            startActivity(new Intent(IntroIpipActivity.this, IntroCompleteActivity.class));
            return;
        }

        // Reset button states
        for (int i = 0; i < _buttons.length; i++) {
            Button btn = _buttons[i];
            btn.setBackgroundColor(Color.rgb(171, 180, 186));
        }

        // Increment to next question
        _currentQuestionIndex++;
        _nextButton.setEnabled(false);
        _label.setText(_questions[_currentQuestionIndex]);
        _score = 0;
        ExperimentData.getInstance(this).addTimeMarker("IntroIpip", "MoveToQuestion" + (_currentQuestionIndex + 1));
    }

    public void onAnswerButtonPress(View v) {
        ExperimentData.getInstance(this).addTimeMarker("IntroIpip", "Question" + (_currentQuestionIndex + 1) + ".Select-" + Integer.toString(GetButton((Button)v)));
        _nextButton.setEnabled(true);
        _score = GetScore((Button)v);

        Button b = (Button)v;
        for (int i = 0; i < _buttons.length; i++) {
            Button btn = _buttons[i];
            btn.setBackgroundColor(btn == b ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        }
    }

    private int GetButton(Button b)
    {
        for (int i = 0; i < 5; i++)
            if (_buttons[i] == b)
                return i + 1;
        return -1;
    }

    private int GetScore(Button b)
    {
        int button = GetButton(b);
        if (_isScoringReverse[_currentQuestionIndex])
            return 6 - button;
        return button;
    }
}