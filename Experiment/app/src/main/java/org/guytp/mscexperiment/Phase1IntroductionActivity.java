package org.guytp.mscexperiment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Phase1IntroductionActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase1_introduction);
        ExperimentData.getInstance().addTimeMarker("Phase1Introduction", "Show");
    }

    public void onBeginPress(View v) {
        ExperimentData.getInstance().addTimeMarker("Phase1Introduction", "Finish");
        startActivity(new Intent(Phase1IntroductionActivity.this, Phase1ExperimentActivity.class));
    }
}