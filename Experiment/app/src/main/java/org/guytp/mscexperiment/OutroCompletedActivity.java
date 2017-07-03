package org.guytp.mscexperiment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OutroCompletedActivity extends KioskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hookup to UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outro_completed);

        // Log this
        ExperimentData.getInstance().addTimeMarker("OutroCompleted", "Show");

        // Terminate bluetooth gracefully
        CushionController.getInstance(this).terminate();
    }
}
