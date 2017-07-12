package org.guytp.mscexperiment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Phase1BreakActivity extends KioskActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase1_break);
        ExperimentData.getInstance(this).addTimeMarker("Phase1Break", "Show");
    }

    public void onContinuePress(View v) {
        ExperimentData.getInstance(this).addTimeMarker("Phase1Break", "Finish");
        startActivity(new Intent(Phase1BreakActivity.this, Phase1ExperimentActivity.class));
    }
}
