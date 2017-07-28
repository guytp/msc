package org.guytp.mscexperiment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.File;

public class AdminMenu extends RelativeLayout {
    private Button _newParticipantButton;
    private Button _phase1Button;
    private Button _phase2Button;
    private Button _phase3Button;
    private Button _timeButton;
    private Button _emailDataButton;
    private Button _exitButton;
    private Activity _activity;
    private AlertDialog _dialog;

    public AdminMenu(Context context) {
        super(context);
        init();
    }

    public AdminMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdminMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.admin_menu, this);
        _newParticipantButton = (Button)findViewById(R.id.newParticipantButton);
        _newParticipantButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onNewParticipantButton(); } });
        _phase1Button = (Button)findViewById(R.id.phase1Button);
        _phase1Button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onPhaseButton(1); } });        _phase2Button = (Button)findViewById(R.id.phase2Button);
        _phase2Button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onPhaseButton(2); } });
        _phase3Button = (Button)findViewById(R.id.phase3Button);
        _phase3Button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onPhaseButton(3); } });
        _timeButton = (Button)findViewById(R.id.timeButton);
        _timeButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { toggleTimeMode(); } });
        _emailDataButton = (Button)findViewById(R.id.emailDataButton);
        _emailDataButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { emailData(); } });
        _exitButton = (Button)findViewById(R.id.exitButton);
        _exitButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            ExperimentData.getInstance(getContext()).addTimeMarker("AdminAction", "Exit");
            android.os.Process.killProcess(android.os.Process.myPid());
            _dialog.dismiss();
            _activity.finish();
        } });
    }

    private void setup(Activity activity, AlertDialog dialog) {
        _activity = activity;
        _dialog = dialog;
        if (isQuickMode())
            _timeButton.setText("Time: Normal");
        else
            _timeButton.setText("Time: Quick");
    }

    private void onNewParticipantButton() {
        _dialog.dismiss();
        ExperimentData.getInstance(getContext()).addTimeMarker("AdminAction", "NewParticipant");
        Intent intent = new Intent();
        intent.setAction("org.guytp.mscexperiment.ParticipantDetailsActivity");
        _activity.startActivity(intent);
    }
    private void onPhaseButton(int phase) {
        _dialog.dismiss();
        ExperimentData.getInstance(getContext()).addTimeMarker("AdminAction", "Phase" + phase);
        Intent intent = new Intent();
        intent.setAction("org.guytp.mscexperiment.Phase" + phase + "IntroductionActivity");
        _activity.startActivity(intent);
    }

    private void toggleTimeMode() {
        if (isQuickMode()) {
            // Revert to normal
            Phase1ExperimentActivity._stateDuration = 5;
            Phase1ExperimentActivity._firstSecondPause = 5;
            Phase1ExperimentActivity._offDelay = 2;
            Phase2ExperimentActivity._stateDuration = 10;
            Phase2ExperimentActivity._maximumStates = 20;
            Phase2ExperimentActivity._offDelay = 2;
            Phase3HoldCushionActivity._durationSeconds = 90;
            Phase3HoldCushionActivity._fadeAfter = 30;
            Phase3HoldCushionActivity._fadeDuration = 10;
            _timeButton.setText("Time: Quick");
        } else {
            Phase1ExperimentActivity._offDelay = 0.5;
            Phase1ExperimentActivity._stateDuration = 1;
            Phase1ExperimentActivity._firstSecondPause = 1;
            Phase2ExperimentActivity._stateDuration = 1;
            Phase2ExperimentActivity._maximumStates = 10;
            Phase2ExperimentActivity._offDelay = 0.5;
            Phase3HoldCushionActivity._durationSeconds = 10;
            Phase3HoldCushionActivity._fadeAfter = 2;
            Phase3HoldCushionActivity._fadeDuration = 1;
            _timeButton.setText("Time: Normal");
        }
        ExperimentData.getInstance(getContext()).addTimeMarker("AdminAction", "ToggleTime-" + (isQuickMode() ? "Quick" : "Normal"));
        _dialog.dismiss();
    }

    private void emailData() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), ExperimentData.getInstance(getContext()).filename());
        Intent emailClient = new Intent(Intent.ACTION_SENDTO, Uri.parse("Guy.Powell@brl.ac.uk"));
        emailClient.putExtra(Intent.EXTRA_SUBJECT, "Participant Record JSON: " + ExperimentData.getInstance(getContext()).filename());
        emailClient.putExtra(Intent.EXTRA_TEXT, ExperimentData.getInstance(getContext()).asString());
        emailClient.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        Intent emailChooser = Intent.createChooser(emailClient, "select email client");
        getContext().startActivity(emailChooser);
    }

    private Boolean isQuickMode() {
        return Phase2ExperimentActivity._stateDuration < 10 && Phase1ExperimentActivity._stateDuration < 10;
    }

    public static void displayAdminPopup(final Activity activity) {
        ExperimentData.getInstance(activity).addTimeMarker("AdminAction", "PopupShow");
        final AdminMenu menu = new AdminMenu(activity);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Admin Options")
                .setMessage("If you are an experiment participant and got here accidentally, please press the close button.")
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExperimentData.getInstance(activity).addTimeMarker("AdminAction", "PopupClose");
                    }
                })
                .setView(menu)
                .show();
        menu.setup(activity, dialog);
    }


}