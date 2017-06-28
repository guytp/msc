package org.guytp.mscexperiment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Phase2IntroductionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase2_introduction);
        ExperimentData.getInstance().addTimeMarker("Phase2Introduction", "Show");
    }
    public void onBeginPress(View v) {
        ExperimentData.getInstance().addTimeMarker("Phase2Introduction", "Finish");
        startActivity(new Intent(Phase2IntroductionActivity.this, Phase2ExperimentActivity.class));
    }
}
