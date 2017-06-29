package org.guytp.mscexperiment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Phase3ExperimentActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_experiment);

        // Setup that phase 3 experiment has started
        ExperimentData.getInstance().addTimeMarker("Phase3Experiment", "Show");
    }

    // TODO: Handle Phase3Experiment.Finish timestamp
}
