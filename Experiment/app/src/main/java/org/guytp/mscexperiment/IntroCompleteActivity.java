package org.guytp.mscexperiment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class IntroCompleteActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_complete);
        ExperimentData.getInstance(this).addTimeMarker("IntroComplete", "Show");
    }

    public void onContinuePress(View v) {
        ExperimentData.getInstance(this).addTimeMarker("IntroComplete", "Finish");
        startActivity(new Intent(IntroCompleteActivity.this, Phase1IntroductionActivity.class));
    }
}
