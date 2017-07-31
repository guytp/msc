package org.guytp.mscexperiment;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by guytp on 23/06/17.
 */

public class ExperimentData {
    private static ExperimentData _applicationInstance = new ExperimentData();
    private String _sessionId;
    private UUID _uuid;
    private List<TimeMarker> _timeMarkers = new ArrayList<TimeMarker>();
    private List<ExperimentDataEntry> _data = new ArrayList<ExperimentDataEntry>();
    private Context _context;
    private Date _lastUpdated;
    private Date _lastSaved;
    private Thread _writingThread;

    private ExperimentData() {
        _sessionId = "Uninitialised";
        _uuid = UUID.randomUUID();
        _lastSaved = new Date();
        _lastUpdated = new Date();
        _writingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (_lastUpdated.after(_lastSaved)) {
                        writeToDisk();
                    }
                    // Wait to check again
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        _writingThread.start();


    }

    public static ExperimentData getInstance(Context context) {
        _applicationInstance._context = context;
        return _applicationInstance;
    }

    public void setSessionId(String value) {
        _sessionId = value;
        _uuid = UUID.randomUUID();
        _timeMarkers.clear();
        _data.clear();
        _lastUpdated = new Date();
    }

    public void addTimeMarker(String category, String action) {
        _timeMarkers.add(new TimeMarker(category, action));
        _lastUpdated = new Date();
    }

    public void addData(String key, String value) {
        _data.add(new ExperimentDataEntry(key, value));
        _lastUpdated = new Date();
    }

    public String getData(String key) {
        for (int i = 0; i < _data.size(); i++)
            if (_data.get(i).key().equals(key))
                return _data.get(i).value();
        return null;
    }

    public String filename() {
        return _uuid.toString() + "_" + _sessionId + ".json";
    }

    public void writeToDisk() {
        String text = asString();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename());
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes());
            outputStream.close();
            _lastSaved = new Date();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String asString() {
        String res = "{\"SessionId\": \"" + _sessionId + "\", \"Uuid\": \"" + _uuid.toString() + "\", \"TimeMarkers\":[";
        for (int i = 0; i < _timeMarkers.size(); i++) {
            if (i != 0)
                res += ", ";
            TimeMarker tm = _timeMarkers.get(i);
            res += "{\"Category\": \"" + tm.category() + "\", \"Action\": \"" + tm.action() + "\", \"Date\": \"" + tm.date() + "\"}";
        }
        res += "], \"Data\":[";
        for (int i = 0; i < _data.size(); i++) {
            if (i != 0)
                res += ", ";
            ExperimentDataEntry d = _data.get(i);
            res += "{\"Key\": \"" + d.key() + "\", \"Value\": \"" + d.value() + "\"}";
        }
        res += "]}";
        return res;
    }
}
