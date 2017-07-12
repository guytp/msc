package org.guytp.mscexperiment;

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

    private ExperimentData() {
    }

    public static ExperimentData getInstance() {
        return _applicationInstance;
    }

    public void setSessionId(String value) {
        _sessionId = value;
        _uuid = UUID.randomUUID();
    }

    public void addTimeMarker(String category, String action) {
        _timeMarkers.add(new TimeMarker(category, action));
    }

    public void addData(String key, String value) {
        _data.add(new ExperimentDataEntry(key, value));
    }

    public String getData(String key) {
        for (int i = 0; i < _data.size(); i++)
            if (_data.get(i).key().equals(key))
                return _data.get(i).value();
        return null;
    }
}
