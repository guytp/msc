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
    private Button _nextButton;
    private View[] _views;
    private int _totalQuestions = 5;
    private Button[] _ageButtons;
    private String _selectedAge = null;
    private Button[] _genderButtons;
    private String _selectedGender = null;
    private Button[] _ethnicityButtons;
    private String _selectedEthnicity = null;
    private Button[] _educationButtons;
    private String _selectedEducation= null;
    private Button[] _researchStudyButtons;
    private String _selectedResearchStudy = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_demographics);

        // Get a handle to the next button
        _nextButton = (Button)findViewById(R.id.nextButton);

        // Get a handle to all question wrapper views
        _views = new View[_totalQuestions];
        _views[0] = findViewById(R.id.question1);
        _views[1] = findViewById(R.id.question2);
        _views[2] = findViewById(R.id.question3);
        _views[3] = findViewById(R.id.question4);
        _views[4] = findViewById(R.id.question5);

        // Set up all but first as invisible
        for (int i = 0; i < _totalQuestions; i++)
            _views[i].setVisibility(i == 0 ? View.VISIBLE : View.GONE);

        // Get a handle to all question buttons
        _ageButtons = new Button[6];
        _ageButtons[0] = (Button)findViewById(R.id.ageButton1);
        _ageButtons[1] = (Button)findViewById(R.id.ageButton2);
        _ageButtons[2] = (Button)findViewById(R.id.ageButton3);
        _ageButtons[3] = (Button)findViewById(R.id.ageButton4);
        _ageButtons[4] = (Button)findViewById(R.id.ageButton5);
        _ageButtons[5] = (Button)findViewById(R.id.ageButton6);
        setupButtonArray(_ageButtons);
        _genderButtons = new Button[4];
        _genderButtons[0] = (Button)findViewById(R.id.genderButton1);
        _genderButtons[1] = (Button)findViewById(R.id.genderButton2);
        _genderButtons[2] = (Button)findViewById(R.id.genderButton3);
        _genderButtons[3] = (Button)findViewById(R.id.genderButton4);
        setupButtonArray(_genderButtons);
        _ethnicityButtons = new Button[14];
        _ethnicityButtons[0] = (Button)findViewById(R.id.ethnicityButton1);
        _ethnicityButtons[1] = (Button)findViewById(R.id.ethnicityButton2);
        _ethnicityButtons[2] = (Button)findViewById(R.id.ethnicityButton3);
        _ethnicityButtons[3] = (Button)findViewById(R.id.ethnicityButton4);
        _ethnicityButtons[4] = (Button)findViewById(R.id.ethnicityButton5);
        _ethnicityButtons[5] = (Button)findViewById(R.id.ethnicityButton6);
        _ethnicityButtons[6] = (Button)findViewById(R.id.ethnicityButton7);
        _ethnicityButtons[7] = (Button)findViewById(R.id.ethnicityButton8);
        _ethnicityButtons[8] = (Button)findViewById(R.id.ethnicityButton9);
        _ethnicityButtons[9] = (Button)findViewById(R.id.ethnicityButton10);
        _ethnicityButtons[10] = (Button)findViewById(R.id.ethnicityButton11);
        _ethnicityButtons[11] = (Button)findViewById(R.id.ethnicityButton12);
        _ethnicityButtons[12] = (Button)findViewById(R.id.ethnicityButton13);
        _ethnicityButtons[13] = (Button)findViewById(R.id.ethnicityButton14);
        setupButtonArray(_ethnicityButtons);
        _educationButtons = new Button[5];
        _educationButtons[0] = (Button)findViewById(R.id.educationButton1);
        _educationButtons[1] = (Button)findViewById(R.id.educationButton2);
        _educationButtons[2] = (Button)findViewById(R.id.educationButton3);
        _educationButtons[3] = (Button)findViewById(R.id.educationButton4);
        _educationButtons[4] = (Button)findViewById(R.id.educationButton5);
        setupButtonArray(_educationButtons);
        _researchStudyButtons = new Button[15];
        _researchStudyButtons[0] = (Button)findViewById(R.id.researchStudyButton1);
        _researchStudyButtons[1] = (Button)findViewById(R.id.researchStudyButton2);
        _researchStudyButtons[2] = (Button)findViewById(R.id.researchStudyButton3);
        _researchStudyButtons[3] = (Button)findViewById(R.id.researchStudyButton4);
        _researchStudyButtons[4] = (Button)findViewById(R.id.researchStudyButton5);
        _researchStudyButtons[5] = (Button)findViewById(R.id.researchStudyButton6);
        _researchStudyButtons[6] = (Button)findViewById(R.id.researchStudyButton7);
        _researchStudyButtons[7] = (Button)findViewById(R.id.researchStudyButton8);
        _researchStudyButtons[8] = (Button)findViewById(R.id.researchStudyButton9);
        _researchStudyButtons[9] = (Button)findViewById(R.id.researchStudyButton10);
        _researchStudyButtons[10] = (Button)findViewById(R.id.researchStudyButton11);
        _researchStudyButtons[11] = (Button)findViewById(R.id.researchStudyButton12);
        _researchStudyButtons[12] = (Button)findViewById(R.id.researchStudyButton13);
        _researchStudyButtons[13] = (Button)findViewById(R.id.researchStudyButton14);
        _researchStudyButtons[14] = (Button)findViewById(R.id.researchStudyButton15);
        setupButtonArray(_researchStudyButtons);

        // Signify start of first question
        ExperimentData.getInstance().addTimeMarker("IntroDemographics-1", "Show");
    }

    public void onNextPress(View v) {
        // Store data from last view and ensure valid data entry
        if (_currentQuestionIndex == 0) {
            if (_selectedAge == null || _selectedAge.length() < 1) {
                dataError("You must select your age");
                return;
            }
            ExperimentData.getInstance().addData("Demographics.Age", _selectedAge);
        }
        else if (_currentQuestionIndex == 1) {
            if (_selectedGender == null || _selectedGender.length() < 1) {
                dataError("You must select which gender you identify as");
                return;
            }
            ExperimentData.getInstance().addData("Demographics.Gender", _selectedGender);
        }
        else if (_currentQuestionIndex == 2) {
            if (_selectedEthnicity == null || _selectedEthnicity.length() < 1) {
                dataError("You must select which ethnicity you identify as");
                return;
            }
            ExperimentData.getInstance().addData("Demographics.Ethnicity", _selectedEthnicity);
        }
        else if (_currentQuestionIndex == 3) {
            if (_selectedEducation == null || _selectedEducation.length() < 1) {
                dataError("You must select your highest level of education");
                return;
            }
            ExperimentData.getInstance().addData("Demographics.Education", _selectedEducation);
        }
        else if (_currentQuestionIndex == 4) {
            if (_selectedResearchStudy == null || _selectedResearchStudy.length() < 1) {
                dataError("You must select which area of research/study/work you partake in");
                return;
            }
            ExperimentData.getInstance().addData("Demographics.ResearchStudyWork", _selectedResearchStudy);
        }

        // Mark this question completed for timing
        ExperimentData.getInstance().addTimeMarker("IntroDemographics-" + _currentQuestionIndex, "Finish");

        // Move to next screen if we're done with questions
        if (_currentQuestionIndex == _totalQuestions - 1)
        {
            startActivity(new Intent(IntroDemographicsActivity.this, IntroPanasActivity.class));
            return;
        }

        // Otherwise show the next question and start a new timer
        _views[_currentQuestionIndex].setVisibility(View.GONE);
        _currentQuestionIndex++;
        _views[_currentQuestionIndex].setVisibility(View.VISIBLE);
        _nextButton.setEnabled(false);
        ExperimentData.getInstance().addTimeMarker("IntroDemographics-" + _currentQuestionIndex, "Show");
    }

    public void onAgeButtonPress(View v) {
        Button b = (Button)v;
        _selectedAge = b.getText().toString();
        setActiveButton(_ageButtons, b);
    }

    public void onGenderButtonPress(View v) {
        Button b = (Button)v;
        _selectedGender = b.getText().toString();
        setActiveButton(_genderButtons, b);
    }

    public void onEthnicityButtonPress(View v) {
        Button b = (Button)v;
        _selectedEthnicity = b.getText().toString();
        setActiveButton(_ethnicityButtons, b);
    }

    public void onEducationButtonPress(View v) {
        Button b = (Button)v;
        _selectedEducation = b.getText().toString();
        setActiveButton(_educationButtons, b);
    }

    public void onResearchStudyButtonPress(View v) {
        Button b = (Button)v;
        _selectedResearchStudy = b.getText().toString();
        setActiveButton(_researchStudyButtons, b);
    }

    private void setupButtonArray(Button[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            Button btn = buttons[i];
            btn.setBackgroundColor(Color.rgb(171, 180, 186));
            btn.setTextColor(Color.BLACK);
        }
    }

    private void setActiveButton(Button[] buttons, Button selectedButton) {
        for (int i = 0; i < buttons.length; i++) {
            Button btn = buttons[i];
            btn.setBackgroundColor(btn == selectedButton ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        }
        _nextButton.setEnabled(true);
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