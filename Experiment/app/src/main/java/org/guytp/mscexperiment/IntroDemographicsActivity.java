package org.guytp.mscexperiment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IntroDemographicsActivity extends KioskActivity {

    private int _currentQuestionIndex = 0;
    private View[] _views;
    private Button[] _ageButtons;
    private int _totalQuestions = 5;
    private String _selectedAge = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_demographics);

        // Get a handle to all views
        _views = new View[_totalQuestions];
        _views[0] = findViewById(R.id.question1);
        _views[1] = findViewById(R.id.question2);
        _views[2] = findViewById(R.id.question3);
        _views[3] = findViewById(R.id.question4);
        _views[4] = findViewById(R.id.question5);

        // Set up all but first as invisible
        for (int i = 1; i < _totalQuestions; i++)
            _views[i].setVisibility(View.GONE);

        // Get a handle to all age buttons
        _ageButtons = new Button[6];
        _ageButtons[0] = (Button)findViewById(R.id.ageButton1);
        _ageButtons[1] = (Button)findViewById(R.id.ageButton2);
        _ageButtons[2] = (Button)findViewById(R.id.ageButton3);
        _ageButtons[3] = (Button)findViewById(R.id.ageButton4);
        _ageButtons[4] = (Button)findViewById(R.id.ageButton5);
        _ageButtons[5] = (Button)findViewById(R.id.ageButton6);
        for (int i = 0; i < _ageButtons.length; i++) {
            Button btn = _ageButtons[i];
            btn.setBackgroundColor(Color.rgb(171, 180, 186));
            btn.setTextColor(Color.BLACK);
        }

        // Signify start of first question
        ExperimentData.getInstance().addTimeMarker("IntroDemographics-1", "Show");
    }

    public void onNextPress(View v) {
        // Store data from last view and ensure valid data entry
        if (_currentQuestionIndex == 1) {
            if (_selectedAge == null || _selectedAge.length() < 1) {
                dataError("You must select your age");
                return;
            }
            ExperimentData.getInstance().addData("Demographics.Age", _selectedAge);
        }
        // TODO: Store

        // Increment our count and either move to next section or show next view
        ExperimentData.getInstance().addTimeMarker("IntroDemographics-" + _currentQuestionIndex, "Finish");
        if (_currentQuestionIndex == _totalQuestions - 1)
        {
            // Move to next screen
            // TODO: Move
            return;
        }

        // Toggle visibility to show latest
        _views[_currentQuestionIndex].setVisibility(View.GONE);
        _currentQuestionIndex++;
        _views[_currentQuestionIndex].setVisibility(View.VISIBLE);
        ExperimentData.getInstance().addTimeMarker("IntroDemographics-" + _currentQuestionIndex, "Show");
    }

    public void onAgeButtonPress(View v) {
        Button b = (Button)v;
        _selectedAge = b.getText().toString();
        for (int i = 0; i < _ageButtons.length; i++) {
            Button btn = _ageButtons[i];
            btn.setBackgroundColor(btn == b ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        }
    }

    private void dataError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setNeutralButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}