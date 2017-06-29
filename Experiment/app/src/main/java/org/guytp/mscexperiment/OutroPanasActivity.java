package org.guytp.mscexperiment;

import android.content.Intent;

public class OutroPanasActivity extends PanasActivity {
    public OutroPanasActivity() {
        _contentViewId = R.layout.activity_outro_panas;
        _loggingPrefix = "Outro";
    }

    @Override
    protected void nextActivity() {
        startActivity(new Intent(OutroPanasActivity.this, OutroCompletedActivity.class));
    }
}
