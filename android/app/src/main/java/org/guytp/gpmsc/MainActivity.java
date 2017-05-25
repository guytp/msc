package org.guytp.gpmsc;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class MainActivity extends Activity implements ColorPickerDialogFragment.ColorPickerDialogListener {
    RadioGroup _lightSolidColourRadioGroup;
    GridLayout _lightCycleControls;
    RadioButton _lightSolidRedRadioButton;
    RadioButton _lightSolidCustomRadioButton;
    Button _happyButton;
    Button _angryButton;
    Button _calmButton;
    Button _attentionSeekingButton;

    int _solidCustomColour = 0xFFFFFFFF;
    BluetoothAdapter _bluetoothAdapter;
    BluetoothDevice _bluetoothDevice;
    BluetoothSocket _bluetoothSocket;
    OutputStream _bluetoothOutputStream;

    int[] _lightCycleColours = new int[10];
    Button[] _lightCycleButtons = new Button[10];
    int _lightCycleColourCount = 4;
    int _lightCycleDuration = 1000;
    int _lightCycleBlend = 25;

    VibrationSetting[] _vibrationSettings;
    short _vibrationCycleDuration = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a handle to required bluetooth context
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_bluetoothAdapter == null) {
            exitWithMessage("Bluetooth is not supported on this device, cannot control cushion.");
            return;
        }
        if (!_bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 1);
        }
        if (!_bluetoothAdapter.isEnabled()) {
            exitWithMessage("You did not enable bluetooth, cannot control cushion.");
            return;
        }
        _bluetoothDevice = _bluetoothAdapter.getRemoteDevice("98:D3:31:FD:4E:FD");
        try {
            _bluetoothSocket = _bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            exitWithMessage("Unable to create socket\r\n" + e.getMessage());
            return;
        }
        try {
            _bluetoothSocket.connect();
            _bluetoothOutputStream = _bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            exitWithMessage("Unable to get bluetooth output stream\r\n" + e.getMessage());
            return;
        }

        // Get a handle to controls we need
        _lightSolidColourRadioGroup = (RadioGroup)findViewById(R.id.lightSolidColourRadioGroup);
        _lightCycleControls = (GridLayout)findViewById(R.id.lightCycleControls);
        _lightSolidRedRadioButton = (RadioButton)findViewById(R.id.lightSolidRedRadioButton);
        _lightSolidCustomRadioButton = (RadioButton)findViewById(R.id.lightSolidCustomRadioButton);
        _lightCycleButtons[0] = (Button)findViewById(R.id.cycleColour1);
        _lightCycleButtons[1] = (Button)findViewById(R.id.cycleColour2);
        _lightCycleButtons[2] = (Button)findViewById(R.id.cycleColour3);
        _lightCycleButtons[3] = (Button)findViewById(R.id.cycleColour4);
        _lightCycleButtons[4] = (Button)findViewById(R.id.cycleColour5);
        _lightCycleButtons[5] = (Button)findViewById(R.id.cycleColour6);
        _lightCycleButtons[6] = (Button)findViewById(R.id.cycleColour7);
        _lightCycleButtons[7] = (Button)findViewById(R.id.cycleColour8);
        _lightCycleButtons[8] = (Button)findViewById(R.id.cycleColour9);
        _lightCycleButtons[9] = (Button)findViewById(R.id.cycleColour10);
        _happyButton = (Button)findViewById(R.id.happyButton);
        _angryButton = (Button)findViewById(R.id.angryButton);
        _calmButton = (Button)findViewById(R.id.calmButton);
        _attentionSeekingButton = (Button)findViewById(R.id.attentionSeekingButton);

        // Handle any of the cycle colour buttons being pressed
        for (int i = 0; i < 10; i++) {
            Button b = _lightCycleButtons[i];
            final int buttonNumber = i + 1;
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialogFragment f = ColorPickerDialogFragment.newInstance(buttonNumber, null, null, _solidCustomColour, false);
                    f.setStyle(DialogFragment.STYLE_NORMAL, R.style.DarkPickerDialogTheme);
                    f.show(getFragmentManager(), "d");
                }
            });
        }

        // Handle number of colours in cycle changing
        SeekBar lightCycleActiveColours = (SeekBar)findViewById(R.id.lightCycleActiveColours);
        lightCycleActiveColours.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                _lightCycleColourCount = progress;
                updateLightCycleColourBoxes();
                sendCycleColourCommand();
            }
        });

        // Handle other cycle colour settings changing
        SeekBar lightCycleBlend = (SeekBar)findViewById(R.id.lightCycleBlend);
        final SeekBar lightCycleDuration = (SeekBar)findViewById(R.id.lightCycleDuration);
        SeekBar.OnSeekBarChangeListener cycleColourSeekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (seekBar == lightCycleDuration)
                    _lightCycleDuration = progress;
                else
                    _lightCycleBlend = progress;
                sendCycleColourCommand();
            }
        };
        lightCycleBlend.setOnSeekBarChangeListener(cycleColourSeekListener);
        lightCycleDuration.setOnSeekBarChangeListener(cycleColourSeekListener);

        // Setup the light mode changing
        RadioGroup lightRadioGroup = (RadioGroup)findViewById(R.id.lightRadioGroup);
        lightRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                LightMode lightMode;
                int solidVisibility = View.GONE;
                int cycleVisibility = View.GONE;
                if (checkedId == R.id.lightSolidRadioButton) {
                    lightMode = LightMode.Solid;
                    solidVisibility = View.VISIBLE;
                }
                else if (checkedId == R.id.lightCycleRadioButton) {
                    lightMode = LightMode.Cycle;
                    cycleVisibility = View.VISIBLE;
                }
                else
                    lightMode = LightMode.Off;
                setLightMode(lightMode);
                _lightSolidColourRadioGroup.setVisibility(solidVisibility);
                _lightCycleControls.setVisibility(cycleVisibility);
            }
        });

        // Handle the preset mood buttons being clicked
        View.OnClickListener moodButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button[] buttons = new Button[] { _happyButton, _calmButton, _angryButton, _attentionSeekingButton };
                for (int i = 0; i < 4; i++) {
                    Button b = buttons[i];
                    if (b == v)
                        b.setBackgroundColor(Color.GREEN);
                    else
                        b.setBackground(null);
                }
                if (v == _happyButton)
                    setMoodHappy();
                else if (v == _calmButton)
                    setMoodCalm();
                else if (v == _angryButton)
                    setMoodAngry();
                else
                    setMoodAttentionSeeking();
            }
        };
        _happyButton.setOnClickListener(moodButtonListener);
        _calmButton.setOnClickListener(moodButtonListener);
        _angryButton.setOnClickListener(moodButtonListener);
        _attentionSeekingButton.setOnClickListener(moodButtonListener);

        // Setup support for custom colours for solid light pattern
        Button lightSolidCustomRadioButton = (Button)findViewById(R.id.lightSolidCustomRadioButton);
        lightSolidCustomRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogFragment f = ColorPickerDialogFragment.newInstance(20, null, null, _solidCustomColour, false);
                f.setStyle(DialogFragment.STYLE_NORMAL, R.style.DarkPickerDialogTheme);
                f.show(getFragmentManager(), "d");
            }
        });

        // Setup a solid colour being selected
        RadioGroup lightSolidColourRadioGroup = (RadioGroup)findViewById(R.id.lightSolidColourRadioGroup);
        lightSolidColourRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.lightSolidCustomRadioButton)
                    return;
                RadioButton button = (RadioButton)findViewById(checkedId);
                setSolidColour(button.getText().toString());
            }
        });

        // Start default mode and set UI defaults to match
        _lightCycleColours[0] = 0xFFFF0000;
        _lightCycleColours[1] = 0xFF00FF00;
        _lightCycleColours[2] = 0xFF0000FF;
        _lightCycleColours[3] = 0xFFFF0000;
        for (int i = 4; i < 10; i++)
            _lightCycleColours[i] = 0xFFFFFFFF;
        updateLightCycleColourBoxes();
        setLightMode(LightMode.Cycle);
        updateSolidColourButtonBackground();
        _lightSolidColourRadioGroup.setVisibility(View.GONE);
        _lightCycleControls.setVisibility(View.VISIBLE);
        _vibrationSettings = new VibrationSetting[9];
        for (int i = 0; i < 9; i++) {
            _vibrationSettings[i] = new VibrationSetting();
            _vibrationSettings[i].numberValues = 3;
            _vibrationSettings[i].values[0].start = 1500;
            _vibrationSettings[i].values[0].end = 2500;
            _vibrationSettings[i].values[0].duration = 750;
            _vibrationSettings[i].values[1].start = 2500;
            _vibrationSettings[i].values[1].end = 1500;
            _vibrationSettings[i].values[1].duration = 750;
            _vibrationSettings[i].values[2].start = 1500;
            _vibrationSettings[i].values[2].end = 1500;
            _vibrationSettings[i].values[2].duration = 100;
        }
        sendVibrationSettings();
    }

    private void updateLightCycleColourBoxes() {
        for (int i = 0; i < 10; i++) {
            Button b = _lightCycleButtons[i];
            b.setVisibility(i < _lightCycleColourCount ? View.VISIBLE : View.INVISIBLE);
            b.setBackgroundColor(_lightCycleColours[i]);
        }
    }

    private void exitWithMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Fatal Error")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void setLightMode(LightMode lightMode) {
        // Sent a bluetooth command requesting light mode
        if (lightMode == LightMode.Solid) {
            setSolidColour(_lightSolidRedRadioButton.getText().toString());
            _lightSolidRedRadioButton.setChecked(true);
        }
        else if (lightMode == LightMode.Cycle)
            sendCycleColourCommand();
        else
            sendBluetoothCommand("LO");
    }

    private void sendBluetoothCommand(String command) {
        sendBluetoothCommand(command.getBytes());
    }

    private void sendBluetoothCommand(byte[] command) {
        try {
            for (int i = 0; i < command.length; i++) {
                _bluetoothOutputStream.write(command[i]);
                Thread.sleep(2);
            }
            _bluetoothOutputStream.write("\n".getBytes());
        } catch (IOException e) {
            exitWithMessage("Error writing to bluetooth device\r\n" + e.getMessage());
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onColorSelected(int dialogId, int colour) {
        if (dialogId == 20) {
            _solidCustomColour = colour;
            updateSolidColourButtonBackground();
            setSolidColour(_solidCustomColour);
            return;
        }

        _lightCycleColours[dialogId - 1] = colour;
        updateLightCycleColourBoxes();
        sendCycleColourCommand();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void setSolidColour(String rgb) {
        setSolidColour(Integer.parseInt(rgb, 16));
    }

    private void setSolidColour(int colour) {
        byte[] buffer = new byte[5];
        buffer[0] = 'L';
        buffer[1] = 'S';
        buffer[2] = (byte)((colour & 0xFF0000) >> 16);
        buffer[3] = (byte)((colour & 0xFF00) >> 8);
        buffer[4] = (byte)(colour & 0xFF);
        sendBluetoothCommand(buffer);
    }

    private void sendCycleColourCommand() {
        // Format LC [4 bytes: duration] [4 bytes: blend] [1 byte: # colours] [3 bytes: colour 1] ... [3 bytes: colour 10]
        // = 41 bytes
        int blendValue = (int)(((double)_lightCycleBlend / (double)100) * (double)_lightCycleDuration);
        byte[] buffer = new byte[41];
        buffer[0] = 'L';
        buffer[1] = 'C';
        buffer[2] = (byte)((_lightCycleDuration & 0xFF000000) >> 24);
        buffer[3] = (byte)((_lightCycleDuration & 0x00FF0000) >> 16);
        buffer[4] = (byte)((_lightCycleDuration & 0x0000FF00) >> 8);
        buffer[5] = (byte)(_lightCycleDuration & 0x000000FF);
        buffer[6] = (byte)((blendValue & 0xFF000000) >> 24);
        buffer[7] = (byte)((blendValue & 0x00FF0000) >> 16);
        buffer[8] = (byte)((blendValue & 0x0000FF00) >> 8);
        buffer[9] = (byte)(blendValue & 0x000000FF);
        buffer[10] = (byte)(_lightCycleColourCount & 0x000000FF);
        for (int offset = 11, i = 0; i < 10; i++, offset += 3) {
            int c = _lightCycleColours[i];
            buffer[offset] = (byte)((c & 0x00FF0000) >> 16);
            buffer[offset + 1] = (byte)((c & 0x0000FF00) >> 8);
            buffer[offset + 2] = (byte)(c & 0x000000FF);
        }
        sendBluetoothCommand(buffer);
    }

    private void updateSolidColourButtonBackground() {
        _lightSolidCustomRadioButton.setBackgroundColor(_solidCustomColour);
    }

    private void sendVibrationSettings() {
        byte[] buffer = new byte[282];
        int offset = 1;
        addShortToBuffer(_vibrationCycleDuration, buffer, offset);
        offset += 2;

        for (int motorIndex = 0 ; motorIndex < 9; motorIndex++ ) {
            buffer[offset] = _vibrationSettings[motorIndex].numberValues;
            offset++;

            // Now add each of the five animation blocks
            for (int i = 0; i < 5; i++) {
                addShortToBuffer(_vibrationSettings[motorIndex].values[i].start, buffer, offset);
                offset += 2;
                addShortToBuffer(_vibrationSettings[motorIndex].values[i].end, buffer, offset);
                offset += 2;
                addShortToBuffer(_vibrationSettings[motorIndex].values[i].duration, buffer, offset);
                offset += 2;
            }
        }
        buffer[0] = 'V';
        sendBluetoothCommand(buffer);
    }

    private void addShortToBuffer(short value, byte[] buffer, int offset) {
        byte leftBit = (byte)((value >> 8) & 0xFF);
        byte rightBit = (byte)(value & 0xFF);
        buffer[offset] = leftBit;
        buffer[offset + 1] = rightBit;
    }

    private void setMoodHappy() {
        _lightCycleColours[0] = 0XFFFF008C;
        _lightCycleColours[1] = 0xFFFF003F;
        _lightCycleColours[2] = 0XFFFF008C;
        _lightCycleColourCount = 3;
        _lightCycleDuration = 6000;
        _lightCycleBlend = 1;
        _vibrationCycleDuration = 1900;
        for (int i = 0; i < 9; i++) {
            _vibrationSettings[i].numberValues = 4;
            _vibrationSettings[i].values[0].start = 2800;
            _vibrationSettings[i].values[0].end = 3500;
            _vibrationSettings[i].values[0].duration = 800;
            _vibrationSettings[i].values[1].start = 3500;
            _vibrationSettings[i].values[1].end = 2800;
            _vibrationSettings[i].values[1].duration = 600;
            _vibrationSettings[i].values[2].start = 1500;
            _vibrationSettings[i].values[2].end = 1500;
            _vibrationSettings[i].values[2].duration = 400;
            _vibrationSettings[i].values[3].start = 0;
            _vibrationSettings[i].values[3].end = 0;
            _vibrationSettings[i].values[3].duration = 100;
        }
        sendVibrationSettings();
        sendCycleColourCommand();
        Toast.makeText(this, "Happy cushion", Toast.LENGTH_LONG).show();
    }

    private void setMoodAngry() {
        _lightCycleColours[0] = 0xFFFF0000;
        _lightCycleColours[1] = 0xFFff2600;
        _lightCycleColours[2] = 0xFFFF0000;
        _lightCycleColourCount = 3;
        _lightCycleDuration = 1000;
        _lightCycleBlend = 20;
        _vibrationCycleDuration = 1500;
        for (int i = 0; i < 9; i++) {
            _vibrationSettings[i].numberValues = 5;
            _vibrationSettings[i].values[0].start = 4095;
            _vibrationSettings[i].values[0].end = 4095;
            _vibrationSettings[i].values[0].duration = 200;
            _vibrationSettings[i].values[1].start = 0;
            _vibrationSettings[i].values[1].end = 0;
            _vibrationSettings[i].values[1].duration = 200;
            _vibrationSettings[i].values[2].start = 4095;
            _vibrationSettings[i].values[2].end = 4095;
            _vibrationSettings[i].values[2].duration = 400;
            _vibrationSettings[i].values[3].start = 0;
            _vibrationSettings[i].values[3].end = 0;
            _vibrationSettings[i].values[3].duration = 200;
            _vibrationSettings[i].values[4].start = 4095;
            _vibrationSettings[i].values[4].end = 0;
            _vibrationSettings[i].values[4].duration = 500;
        }
        sendVibrationSettings();
        sendCycleColourCommand();
        Toast.makeText(this, "Uh oh!  Angry cushion!", Toast.LENGTH_LONG).show();
    }

    private void setMoodCalm() {
        _lightCycleColours[0] = 0xFF00a5ff;
        _lightCycleColours[1] = 0xFF54ff00;
        _lightCycleColours[2] = 0xFF00a5ff;
        _lightCycleColourCount = 3;
        _lightCycleDuration = 6000;
        _lightCycleBlend = 20;
        _vibrationCycleDuration = 1500;
        for (int i = 0; i < 9; i++) {
            _vibrationSettings[i].numberValues = 3;
            _vibrationSettings[i].values[0].start = 1500;
            _vibrationSettings[i].values[0].end = 2500;
            _vibrationSettings[i].values[0].duration = 800;
            _vibrationSettings[i].values[1].start = 2500;
            _vibrationSettings[i].values[1].end = 1500;
            _vibrationSettings[i].values[1].duration = 600;
            _vibrationSettings[i].values[2].start = 0;
            _vibrationSettings[i].values[2].end = 0;
            _vibrationSettings[i].values[2].duration = 100;
        }
        sendVibrationSettings();
        sendCycleColourCommand();
        Toast.makeText(this, "Cushion is zen", Toast.LENGTH_LONG).show();
    }

    private void setMoodAttentionSeeking() {
        _lightCycleColours[0] = 0xFFc700ff;
        _lightCycleColours[1] = 0xFFc700ff;
        _lightCycleColours[2] = 0xFFc700ff;
        _lightCycleColours[3] = 0xFFc700ff;
        _lightCycleColours[4] = 0xFFc700ff;
        _lightCycleColours[5] = 0xFFc700ff;
        _lightCycleColours[6] = 0xFFFF0000;
        _lightCycleColours[7] = 0xFF00FF00;
        _lightCycleColours[8] = 0xFF0000FF;
        _lightCycleColourCount = 9;
        _lightCycleDuration = 2000;
        _lightCycleBlend = 20;
        _vibrationCycleDuration = 2000;
        for (int i = 0; i < 9; i++) {
            boolean isCycle1 = i == 0 || i == 3 || i == 6;
            boolean isCycle2 = i == 1 || i == 4 || i == 7;
            boolean isCycle3 = !isCycle1 && !isCycle2;
            _vibrationSettings[i].numberValues = 5;
            _vibrationSettings[i].values[0].start = 4095;
            _vibrationSettings[i].values[0].end = 4095;
            _vibrationSettings[i].values[0].duration = 550;
            _vibrationSettings[i].values[1].start = 4095;
            _vibrationSettings[i].values[1].end = 2500;
            _vibrationSettings[i].values[1].duration = 550;
            _vibrationSettings[i].values[2].start = (short)(isCycle1 ? 4095 : 0);
            _vibrationSettings[i].values[2].end = (short)(isCycle1 ? 4095 : 0);
            _vibrationSettings[i].values[2].duration = 300;
            _vibrationSettings[i].values[3].start = (short)(isCycle2 ? 4095 : 0);
            _vibrationSettings[i].values[3].end = (short)(isCycle2 ? 4095 : 0);
            _vibrationSettings[i].values[3].duration = 300;
            _vibrationSettings[i].values[4].start = (short)(isCycle3 ? 4095 : 0);
            _vibrationSettings[i].values[4].end = (short)(isCycle3 ? 4095 : 0);
            _vibrationSettings[i].values[4].duration = 300;
        }
        sendVibrationSettings();
        sendCycleColourCommand();
        Toast.makeText(this, "Cushion needs attention", Toast.LENGTH_LONG).show();
    }
}