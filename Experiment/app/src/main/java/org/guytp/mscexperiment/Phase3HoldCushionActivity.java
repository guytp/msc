package org.guytp.mscexperiment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.Random;

public class Phase3HoldCushionActivity extends KioskActivity {

    public static double _durationSeconds = 90;

    public static double _fadeAfter = 30;

    public static double _fadeDuration = 10;

    private TextView _label;

    private Handler _timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hookup to UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_hold_cushion);
        _label = (TextView)findViewById(R.id.label);

        // Read out which states the user selected for calm and angry at random with 50% chance
        // of either being used then display this state
        String chosenStateType = new Random().nextDouble() < 0.5 ? "Angry" : "Calm";
        String stateWordToUse = ExperimentData.getInstance(this).getData("Phase3Experiment." + chosenStateType + "State");
        CushionState state;
        if (stateWordToUse.equals("Angry"))
            state = CushionState.Angry;
        else if (stateWordToUse.equals("Calm"))
            state = CushionState.Calm;
        else if (stateWordToUse.equals("Happy"))
            state = CushionState.Happy;
        else
            state = CushionState.Sad;
        ExperimentData.getInstance(this).addData("Phase3Cushion.State.Chosen", chosenStateType);
        ExperimentData.getInstance(this).addData("Phase3Cushion.State.DisplayAs", stateWordToUse);
        CushionController.getInstance(this).setState(state);

        // Start a timer to move to the next screen
        _timer = new Handler();
        _timer.postDelayed(new Runnable() { @Override public void run() { fadeLabel(); } }, (int)(_fadeAfter * 1000));

        // Log this
        ExperimentData.getInstance(this).addTimeMarker("Phase3Cushion", "Show");
    }

    private void transitionAway() {
        // Turn cushion off
        CushionController.getInstance(this).off();

        // Log this and transition away to final PANAS questionnaire
        ExperimentData.getInstance(this).addTimeMarker("Phase3Cushion", "Finish");
        startActivity(new Intent(Phase3HoldCushionActivity.this, Phase3FinalQuestionActivity.class));
    }

    private void fadeLabel() {
        // Begin an animation to fade the label
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(_label, "alpha",  1f, 0f);
        fadeOut.setDuration((int)(_fadeDuration * 1000));
        fadeOut.start();

        // Start a timer to move to the next screen
        _timer.postDelayed(new Runnable() { @Override public void run() { transitionAway(); } }, (int)((_durationSeconds - _fadeAfter) * 1000));
    }
}