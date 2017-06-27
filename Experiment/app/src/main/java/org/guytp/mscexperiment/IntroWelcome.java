package org.guytp.mscexperiment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class IntroWelcome extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_welcome);
        ExperimentData.getInstance().addTimeMarker("IntroWelcome", "Show");
    }

    public void onBeginPress(View v) {
        ExperimentData.getInstance().addTimeMarker("IntroWelcome", "Finish");
        startActivity(new Intent(IntroWelcome.this, IntroDemographicsActivity.class));

    }
}