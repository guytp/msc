package org.guytp.mscexperiment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.Random;

public class Phase3HoldCushionActivity extends KioskActivity {

    private final double _durationSeconds = 60;

    private Handler _timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hookup to UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_hold_cushion);

        // Read out which states the user selected for calm and angry at random with 50% chance
        // of either being used then display this state
        String chosenStateType = new Random().nextDouble() < 0.5 ? "Angry" : "Calm";
        String stateWordToUse = ExperimentData.getInstance().getData("Phase3Experiment." + chosenStateType + "State");
        CushionState state;
        if (stateWordToUse.equals("Angry"))
            state = CushionState.Angry;
        else if (stateWordToUse.equals("Calm"))
            state = CushionState.Calm;
        else if (stateWordToUse.equals("Happy"))
            state = CushionState.Happy;
        else
            state = CushionState.Sad;
        ExperimentData.getInstance().addData("Phase3Cushion.State.Chosen", chosenStateType);
        ExperimentData.getInstance().addData("Phase3Cushion.State.DisplayAs", stateWordToUse);
        CushionController.getInstance(this).setState(state);

        // Start a timer to move to the next screen
        _timer = new Handler();
        _timer.postDelayed(new Runnable() { @Override public void run() { transitionAway(); } }, (int)(_durationSeconds * 1000));

        // Log this
        ExperimentData.getInstance().addTimeMarker("Phase3Cushion", "Show");
    }

    private void transitionAway() {
        // Turn cushion off
        CushionController.getInstance(this).off();

        // Log this and transition away to final PANAS questionnaire
        ExperimentData.getInstance().addTimeMarker("Phase3Cushion", "Finish");
        startActivity(new Intent(Phase3HoldCushionActivity.this, OutroPanasActivity.class));
    }
}