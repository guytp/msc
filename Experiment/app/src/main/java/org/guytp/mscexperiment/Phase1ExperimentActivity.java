package org.guytp.mscexperiment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Phase1ExperimentActivity extends KioskActivity {

    public static double _stateDuration = 5;

    public static double _firstSecondPause = 3;

    public static double _offDelay = 0;

    public static int _numberPairs = 10;

    private CushionState[] _cushionStates;

    private int _nextStateToShow = 0;

    private Handler _timer;

    private Runnable _displayStateRunnable;

    private Runnable _turnOffRunnable;
    private Runnable _turnOffRunnableCompletion;
    private Runnable _displayStateRunnableCompletion;

    private CushionController _cushionController;

    private TextView _label;

    private Button _sameButton;
    private Button _differentButton;
    private Button _nextButton;

    private boolean _same;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase1_experiment);

        // Get a handle to controls
        _label = (TextView)findViewById(R.id.mainLabel);
        _sameButton = (Button)findViewById(R.id.sameButton);
        _differentButton = (Button)findViewById(R.id.differentButton);
        _nextButton = (Button)findViewById(R.id.nextButton);
        setButtonVisibilities(View.INVISIBLE);

        // Determine the order of our states - we will have five-passes of random states and get a
        // handler to cushion controller
        _cushionStates = CushionState.generateRandomStates(_numberPairs * 2, 0.23);
        for (int i = 0; i < _cushionStates.length; i++)
            ExperimentData.getInstance(this).addData("Phase1Experiment.State" + (i + 1), _cushionStates[i].toString());
        _cushionController = CushionController.getInstance(this);

        // Store the runnables used for setting a state and turning off
        _displayStateRunnable = new Runnable() { @Override public void run() { displayStateRunnable(); } };
        _turnOffRunnable = new Runnable() { @Override public void run() { turnOffRunnable(); } };
        _turnOffRunnableCompletion = new Runnable() { @Override public void run() { turnOffRunnableCompletion(); } };
        _displayStateRunnableCompletion = new Runnable() { @Override public void run() { displayStateRunnableCompletion(); } };

        // Schedule execution very soon of first state
        _timer = new Handler();
        displayStateRunnable();

        // Mark start of this phase
        ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment", "Show");
    }

    private void displayStateRunnable() {
        // Display in UI a blank screen
        _label.setText("");

        // Trigger to complete turn off in 2 seconds before we transition to next state
        _timer.postDelayed(_displayStateRunnableCompletion, (int)(_offDelay * 1000));
    }

    private void displayStateRunnableCompletion() {
        // Set valid state
        _cushionController.setState(_cushionStates[_nextStateToShow]);

        // Log the time we show the state
        ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment.StateShow" + (_nextStateToShow + 1), "On");

        // Scheduled off in 10 seconds
        _timer.postDelayed(_turnOffRunnable, (int)(_stateDuration * 1000));
    }

    private void turnOffRunnable() {
        // Turn off
        _cushionController.off();
        ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment.StateShow" + (_nextStateToShow + 1), "Off");

        // Increment next state to show
        _nextStateToShow++;

        // Trigger to complete turn off in 2 seconds before we transition to next state
        _timer.postDelayed(_turnOffRunnableCompletion, (int)(_offDelay * 1000));
    }

    private void turnOffRunnableCompletion() {
        // If we're going to the second in a pair then display the "wait a second" pause
        if (_nextStateToShow %2 != 0) {
            _label.setText("Cushion will go to the next state shortly.");
            _timer.postDelayed(_displayStateRunnable, (int)(_firstSecondPause * 1000));
            return;
        }

        // If we've finished a pair transition to a "same or different phase"
        ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment.StateQuestion" + (_nextStateToShow - 1) + "-" + (_nextStateToShow), "Show");
        _label.setText("That was pair " + (_nextStateToShow / 2) + " of " + _numberPairs + ".  Was your experience of these two states the same or different?");
        _sameButton.setVisibility(View.VISIBLE);
        _differentButton.setVisibility(View.VISIBLE);
    }
    public void onSameDifferentPress(View v) {
        _same = v == _sameButton;
        _nextButton.setVisibility(View.VISIBLE);
        _sameButton.setBackgroundColor(v == _sameButton ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        _differentButton.setBackgroundColor(v == _differentButton ? Color.rgb(57, 175, 239) : Color.rgb(171, 180, 186));
        ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment.StateQuestion" + (_nextStateToShow - 1) + "-" + (_nextStateToShow) + ".Press", _same ? "Same" : "Different");
    }

    public void onNextPress(View v) {
        // Record the participant's answer
        ExperimentData.getInstance(this).addData("Phase1Experiment.State" + (_nextStateToShow - 1) + "-" + (_nextStateToShow) + ".States", _cushionStates[_nextStateToShow - 2] + " - " + _cushionStates[_nextStateToShow - 1]);
        ExperimentData.getInstance(this).addData("Phase1Experiment.State" + (_nextStateToShow - 1) + "-" + (_nextStateToShow) + ".Answer", _same ? "Same" : "Different");

        // If we are not on last one then display next state
        if (_nextStateToShow < _cushionStates.length) {
            // Transition to next state
            ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment.StateQuestion" + (_nextStateToShow - 1) + "-" + (_nextStateToShow), "Finish");
            setButtonVisibilities(View.INVISIBLE);
            displayStateRunnable();
            return;
        }

        // If this is the end add marker and transition
        CushionController.getInstance(this).off();
        ExperimentData.getInstance(this).addTimeMarker("Phase1Experiment", "Finish");
        startActivity(new Intent(Phase1ExperimentActivity.this, Phase1CompleteActivity.class));
    }

    private void setButtonVisibilities(int visibility) {
        _sameButton.setVisibility(visibility);
        _differentButton.setVisibility(visibility);
        if (visibility != View.VISIBLE) {
            _nextButton.setVisibility(visibility);
            _sameButton.setBackgroundColor(Color.rgb(171, 180, 186));
            _differentButton.setBackgroundColor(Color.rgb(171, 180, 186));
        }
    }
}