package org.guytp.mscexperiment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

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
        _phase1Button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onPhaseButton(1); } });
        _phase2Button = (Button)findViewById(R.id.phase2Button);
        _phase2Button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onPhaseButton(2); } });
        _phase3Button = (Button)findViewById(R.id.phase3Button);
        _phase3Button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { onPhaseButton(3); } });
        _timeButton = (Button)findViewById(R.id.timeButton);
        _timeButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { toggleTimeMode(); } });
        _emailDataButton = (Button)findViewById(R.id.emailDataButton);
        _exitButton = (Button)findViewById(R.id.exitButton);
        _exitButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            ExperimentData.getInstance().addTimeMarker("AdminAction", "Exit");
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
        ExperimentData.getInstance().addTimeMarker("AdminAction", "NewParticipant");
        Intent intent = new Intent();
        intent.setAction("org.guytp.mscexperiment.ParticipantDetailsActivity");
        _activity.startActivity(intent);
    }
    private void onPhaseButton(int phase) {
        _dialog.dismiss();
        ExperimentData.getInstance().addTimeMarker("AdminAction", "Phase" + phase);
        Intent intent = new Intent();
        intent.setAction("org.guytp.mscexperiment.Phase" + phase + "IntroductionActivity");
        _activity.startActivity(intent);
    }

    private void toggleTimeMode() {
        if (isQuickMode()) {
            // Revert to normal
            Phase1ExperimentActivity._stateDuration = 10;
            Phase1ExperimentActivity._firstSecondPause = 5;
            Phase1ExperimentActivity._offDelay = 2;
            Phase2ExperimentActivity._stateDuration = 10;
            Phase2ExperimentActivity._maximumStates = 20;
            Phase2ExperimentActivity._offDelay = 2;
            _timeButton.setText("Time: Quick");
        } else {
            Phase1ExperimentActivity._offDelay = 0.5;
            Phase1ExperimentActivity._stateDuration = 1;
            Phase1ExperimentActivity._firstSecondPause = 1;
            Phase2ExperimentActivity._stateDuration = 1;
            Phase2ExperimentActivity._maximumStates = 10;
            Phase2ExperimentActivity._offDelay = 0.5;
            _timeButton.setText("Time: Normal");
        }
        ExperimentData.getInstance().addTimeMarker("AdminAction", "ToggleTime-" + (isQuickMode() ? "Quick" : "Normal"));
        _dialog.dismiss();
    }

    private Boolean isQuickMode() {
        return Phase2ExperimentActivity._stateDuration < 10 && Phase1ExperimentActivity._stateDuration < 10;
    }

    public static void displayAdminPopup(Activity activity) {
        ExperimentData.getInstance().addTimeMarker("AdminAction", "PopupShow");
        final AdminMenu menu = new AdminMenu(activity);
        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Admin Options")
                .setMessage("If you are an experiment participant and got here accidentally, please press the close button.")
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExperimentData.getInstance().addTimeMarker("AdminAction", "PopupClose");
                    }
                })
                .setView(menu)
                .show();
        menu.setup(activity, dialog);
    }
}