package org.guytp.gpmsc;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends Activity implements ColorPickerDialogFragment.ColorPickerDialogListener {
    RadioGroup _lightSolidColourRadioGroup;
    GridLayout _lightCycleControls;
    RadioButton _lightSolidRedRadioButton;
    RadioButton _lightSolidCustomRadioButton;

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
            _bluetoothOutputStream.write(command);
            _bluetoothOutputStream.write("\n".getBytes());
        } catch (IOException e) {
            exitWithMessage("Error writing to bluetooth device\r\n" + e.getMessage());
            return;
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
}