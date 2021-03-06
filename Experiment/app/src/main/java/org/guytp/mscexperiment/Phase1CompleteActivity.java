package org.guytp.mscexperiment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Phase1CompleteActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase1_complete);
        ExperimentData.getInstance(this).addTimeMarker("Phase1Complete", "Show");
    }
    public void onContinuePress(View v) {
        ExperimentData.getInstance(this).addTimeMarker("Phase1Complete", "Finish");
        startActivity(new Intent(Phase1CompleteActivity.this, Phase2IntroductionActivity.class));
    }
}
