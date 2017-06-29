package org.guytp.mscexperiment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Phase2CompleteActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase2_complete);
        ExperimentData.getInstance().addTimeMarker("Phase2Complete", "Show");
    }
    public void onContinuePress(View v) {
        ExperimentData.getInstance().addTimeMarker("Phase2Complete", "Finish");
        // TODO: Link to Phase 3
        //startActivity(new Intent(Phase2CompleteActivity.this, Phase3IntroductionActivity.class));
    }
}