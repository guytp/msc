package org.guytp.mscexperiment;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

public class CushionController {
    private static CushionController _applicationInstance;

    private Activity _context;
    private BluetoothAdapter _bluetoothAdapter;
    private BluetoothDevice _bluetoothDevice;
    private BluetoothSocket _bluetoothSocket;
    private OutputStream _bluetoothOutputStream;
    private InputStream _bluetoothInputStream;

    private CushionController(Activity context) {
        // Store context
        _context = context;

        // Get a handle to required bluetooth context
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_bluetoothAdapter == null) {
            displayCriticalMessage("Bluetooth is not supported on this device, cannot control cushion.");
            return;
        }
        if (!_bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            _context.startActivityForResult(enableBluetooth, 1);
        }
        if (!_bluetoothAdapter.isEnabled()) {
            displayCriticalMessage("You did not enable bluetooth, cannot control cushion.");
            return;
        }
        _bluetoothDevice = _bluetoothAdapter.getRemoteDevice("98:D3:31:FD:4E:FD");
        if (_bluetoothDevice == null) {
            displayCriticalMessage("Unable to obtain bluetooth device");
            return;
        }
        try {
            _bluetoothSocket = _bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            if (_bluetoothSocket == null) {
                displayCriticalMessage("Unable to obtain bluetooth socket");
                return;
            }
        } catch (IOException e) {
            displayCriticalMessage("Unable to create socket\r\n" + e.getMessage());
            return;
        }
        try {
            _bluetoothSocket.connect();
            _bluetoothOutputStream = _bluetoothSocket.getOutputStream();
            _bluetoothInputStream = _bluetoothSocket.getInputStream();
            if (_bluetoothOutputStream == null) {
                displayCriticalMessage("Unable to obtain bluetooth output stream");
                return;
            }
            if (_bluetoothInputStream == null) {
                displayCriticalMessage("Unable to obtain bluetooth input stream");
                return;
            }
        } catch (IOException e) {
            displayCriticalMessage("Unable to get bluetooth output stream\r\n" + e.getMessage());
            return;
        }
    }

    private void displayCriticalMessage(String message) {
        displayCriticalMessage(message, true);
    }
    private void displayCriticalMessage(String message, Boolean terminate) {
        final boolean doTerminate = terminate;
        new AlertDialog.Builder(_context)
                .setTitle("Bluetooth Error")
                .setMessage("There appears to be a problem with the cushion.  Please leave the room and get the facilitator and show this message.\r\n\r\n" + message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (doTerminate)
                            _context.finish();
                    }
                })
                .show();
    }

    public static CushionController getInstance(Activity activity) {
        if (_applicationInstance == null)
            _applicationInstance = new CushionController(activity);
        else
            _applicationInstance._context = activity;
        return _applicationInstance;
    }

    public void terminate() {
        try {
            _bluetoothOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            _bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _applicationInstance = null;
    }

    public void setState(CushionState state) {
        sendState((byte)state.rawValue());
    }

    public void off() {
        sendState((byte)0);
    }

    private void sendState(byte state) {
        if (_bluetoothOutputStream == null || _bluetoothInputStream == null)
            return;
        try {
            for (int attempt = 0; attempt < 5; attempt++) {

                byte[] bytes = new byte[3];
                bytes[0] = 0x53;
                bytes[1] = state;
                bytes[2] = '\n';
                for (int i = 0; i < 3; i++) {
                    _bluetoothOutputStream.write(bytes[i]);
                    Thread.sleep(10);
                }

                // Wait for data for up to 100ms
                Date startWait = new Date();
                while (_bluetoothInputStream.available() < 1) {
                    Date now = new Date();
                    long millis = (now.getTime()-startWait.getTime());
                    if (millis > 500)
                        break;
                    Thread.sleep(10);
                }

                Boolean hasRead = false;
                byte lastRead = 0;
                if (_bluetoothInputStream.available() > 0) {
                    hasRead = true;
                    while (_bluetoothInputStream.available() > 0) {
                        lastRead = (byte)_bluetoothInputStream.read();
                    }
                }
                if (hasRead && lastRead == 1)
                    break;
                Log.e("MscBluetooth", "Error - retrying send");
            }
        } catch (IOException e) {
            displayCriticalMessage("Error sending data to cushion\r\n" + e.getMessage(), false);
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}