package org.guytp.mscexperiment;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import java.util.Date;

public class KioskActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        Boolean isInRange = (event.getX() < 250 && event.getY() < 250);
        if (!isInRange)
            return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            return true;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getEventTime() - event.getDownTime() > 1500) {
                AdminMenu.displayAdminPopup(this);
            }
            return true;
        }
        return false;
    }
}