package org.guytp.mscexperiment;

import android.content.Context;

import java.io.FileOutputStream;
import java.util.ArrayList;
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

    private ExperimentData() {
        _sessionId = "Uninitialised";
        _uuid = UUID.randomUUID();
    }

    public static ExperimentData getInstance(Context context) {
        _applicationInstance._context = context;
        return _applicationInstance;
    }

    public void setSessionId(String value) {
        _sessionId = value;
        _uuid = UUID.randomUUID();
        writeToDisk();
    }

    public void addTimeMarker(String category, String action) {
        _timeMarkers.add(new TimeMarker(category, action));
        writeToDisk();
    }

    public void addData(String key, String value) {
        _data.add(new ExperimentDataEntry(key, value));
        writeToDisk();
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
        try {
            FileOutputStream outputStream = _context.openFileOutput(filename(), Context.MODE_WORLD_READABLE);
            outputStream.write(text.getBytes());
            outputStream.close();
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
