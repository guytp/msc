package org.guytp.mscexperiment;

import android.os.Bundle;

public class Phase1ExperimentActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase1_experiment);
        ExperimentData.getInstance().addTimeMarker("Phase1Experiment", "Show");
    }
}
