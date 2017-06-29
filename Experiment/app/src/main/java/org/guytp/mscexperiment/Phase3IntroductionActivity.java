package org.guytp.mscexperiment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Phase3IntroductionActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phase3_introduction);
        ExperimentData.getInstance().addTimeMarker("Phase3Introduction", "Show");
    }
    public void onBeginPress(View v) {
        ExperimentData.getInstance().addTimeMarker("Phase3Introduction", "Finish");
        startActivity(new Intent(Phase3IntroductionActivity.this, Phase3ExperimentActivity.class));
    }
}
