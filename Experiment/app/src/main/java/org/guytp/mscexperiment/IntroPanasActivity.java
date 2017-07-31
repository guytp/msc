package org.guytp.mscexperiment;

import android.content.Intent;

public class IntroPanasActivity extends PanasActivity {
    public IntroPanasActivity() {
        _contentViewId = R.layout.activity_intro_panas;
        _loggingPrefix = "Intro";
    }

    @Override
    protected void nextActivity() {
        startActivity(new Intent(IntroPanasActivity.this, IntroIpipActivity.class));
    }
}
