package org.guytp.mscexperiment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ParticipantDetailsActivity extends KioskActivity {
    private TimePicker _timePicker;
    private DatePicker _datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call to base
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_details);

        // Setup UI components
        Date date = new Date();
        _datePicker = (DatePicker)findViewById(R.id.datePicker);
        //_datePicker.updateDate(date.getYear(), date.getMonth(), date.getDay());
        _timePicker = (TimePicker)findViewById(R.id.timePicker);
        _timePicker.setIs24HourView(true);
        _timePicker.setCurrentHour(date.getHours());
        //_timePicker.setCurrentMinute(date.getMinutes());
    }

    public void onBeginPress(View v) {
        // Determine date from spinners
        Date date = new Date(_datePicker.getYear() - 1900, _datePicker.getMonth(), _datePicker.getDayOfMonth(), _timePicker.getCurrentHour(), _timePicker.getCurrentMinute());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        final String dateFormatted = dateFormat.format(date);

        // Confirm to user
        new AlertDialog.Builder(this)
        .setTitle("Begin Session")
        .setMessage("Are you ready to begin the session with the name " + dateFormatted + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        // Setup experiment data
                        ExperimentData.getInstance().setSessionId(dateFormatted);

                        // Now show the experiment welcome for the participant
                        startActivity(new Intent(ParticipantDetailsActivity.this, IntroWelcome.class));
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}