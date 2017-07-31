package org.guytp.mscexperiment;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Phase2ExperimentActivity extends KioskActivity {

    public static double _stateDuration = 6;

    public static int _maximumStates = 20;

    public static double _offDelay = 0;

    private CushionController _cushionController;

    private CushionState[] _cushionStates;

    private int _nextStateToShow = 0;

    private SeekBar _sliderEnergy;

    private SeekBar _sliderPleasantness;

    private View _answerLayout;

    private TextView _questionLabel;

    private View _stateOnLayout;

    private Handler _timer;

    private Runnable _displayStateRunnable;
    private Runnable _turnOffRunnable;
    private Runnable _turnOffRunnableCompletion;

    private TextView _stateOnLabel;


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

        // Listen to slider events
        final Context ctx = this;
        SeekBar.OnSeekBarChangeListener sliderListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ExperimentData.getInstance(ctx).addTimeMarker("Phase2Experiment.State" + (_nextStateToShow + 1), "SlideStop." + (seekBar == _sliderEnergy ? "Energy" : "Pleasantness"));
                ExperimentData.getInstance(ctx).addData("Phase2Experiment.State" + (_nextStateToShow + 1) + ".NonFinal." + (seekBar == _sliderEnergy ? "Energy" : "Pleasantness"), Integer.toString(_sliderEnergy.getProgress() - 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ExperimentData.getInstance(ctx).addTimeMarker("Phase2Experiment.State" + (_nextStateToShow + 1), "SlideStart." + (seekBar == _sliderEnergy ? "Energy" : "Pleasantness"));
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

            }
        };
        _sliderEnergy.setOnSeekBarChangeListener(sliderListener);
        _sliderPleasantness.setOnSeekBarChangeListener(sliderListener);

        // Determine which states we will show
        _cushionStates = CushionState.randomlyOrderedStatesSets(5);
        if (_cushionStates.length > _maximumStates && _maximumStates > 0) {
            CushionState[] newStates = new CushionState[_maximumStates];
            for (int i = 0; i < _maximumStates; i++)
                newStates[i] = _cushionStates[i];
            _cushionStates = newStates;
        }
        for (int i = 0; i < _cushionStates.length; i++)
            ExperimentData.getInstance(this).addData("Phase2Experiment.State" + (i + 1), _cushionStates[i].toString());
        _cushionController = CushionController.getInstance(this);

        // Hide answering UI and show first question
        _answerLayout.setVisibility(View.GONE);
        _stateOnLayout.setVisibility(View.VISIBLE);

        // Store the runnables used for setting a state and turning off
        _displayStateRunnable = new Runnable() { @Override public void run() { displayStateRunnable(); } };
        _turnOffRunnable = new Runnable() { @Override public void run() { turnOffRunnable(); } };
        _turnOffRunnableCompletion = new Runnable() { @Override public void run() { turnOffRunnableCompletion(); } };
        _timer = new Handler();

        // Schedule execution very soon of first state
        preStatePauseRunnable();

        // Record the start of this screen
        ExperimentData.getInstance(this).addTimeMarker("Phase2Experiment", "Show");
    }

    private void preStatePauseRunnable() {
        // Display in UI - we want a blank screen
        _stateOnLabel.setText("Now, please focus on the cushion");
        _stateOnLabel.setAlpha(0.5f);

        // Wait 2s before we start the state
        _timer.postDelayed(_displayStateRunnable, (int)(_offDelay * 1000));
    }

    private void displayStateRunnable() {
        // Set valid state
        _cushionController.setState(_cushionStates[_nextStateToShow]);

        ExperimentData.getInstance(this).addTimeMarker("Phase2Experiment.StateShow" + (_nextStateToShow + 1), "On");

        // Scheduled off in 10 seconds
        _timer.postDelayed(_turnOffRunnable, (int)(_stateDuration * 1000));
    }

    private void turnOffRunnable() {
        // Turn off
        _cushionController.off();
        ExperimentData.getInstance(this).addTimeMarker("Phase2Experiment.StateShow" + (_nextStateToShow + 1), "Off");

        // Increment next state to show
        _nextStateToShow++;

        // Trigger to complete turn off in 2 seconds before we begin next state
        _timer.postDelayed(_turnOffRunnableCompletion, (int)(_offDelay * 1000));
    }

    private void turnOffRunnableCompletion() {
        // Transition to answer phase
        ExperimentData.getInstance(this).addTimeMarker("Phase2Experiment.StateQuestion" + (_nextStateToShow), "Show");
        _answerLayout.setVisibility(View.VISIBLE);
        _questionLabel.setText("Please move the circles below to indicate how the levels of arousal and \"pleasantness\"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  that you perceived for state " + _nextStateToShow + " of " + _cushionStates.length + ".  Once you're done press the Next button.");
        _stateOnLayout.setVisibility(View.GONE);
    }

    public void onNextPress(View v) {
        // Record the participant's answer
        ExperimentData.getInstance(this).addTimeMarker("Phase2Experiment.StateQuestion" + (_nextStateToShow), "Finish");
        ExperimentData.getInstance(this).addData("Phase2Experiment.State" + (_nextStateToShow) + ".State", _cushionStates[_nextStateToShow - 1].toString());
        ExperimentData.getInstance(this).addData("Phase2Experiment.State" + (_nextStateToShow) + ".Energy", Integer.toString(_sliderEnergy.getProgress() - 100));
        ExperimentData.getInstance(this).addData("Phase2Experiment.State" + (_nextStateToShow) + ".Pleasantness", Integer.toString(_sliderPleasantness.getProgress() - 100));

        // If this is the last state move on to complete activity
        if (_nextStateToShow == _cushionStates.length) {
            CushionController.getInstance(this).off();
            ExperimentData.getInstance(this).addTimeMarker("Phase2Experiment", "Finish");
            startActivity(new Intent(Phase2ExperimentActivity.this, Phase2CompleteActivity.class));
            return;
        }

        // Hide the answer UI and start the next state along with a timer to turn it off
        _answerLayout.setVisibility(View.GONE);
        _stateOnLayout.setVisibility(View.VISIBLE);
        _sliderPleasantness.setProgress(100);
        _sliderEnergy.setProgress(100);
        preStatePauseRunnable();
    }
}
