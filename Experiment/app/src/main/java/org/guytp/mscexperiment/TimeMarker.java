package org.guytp.mscexperiment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeMarker {
    private String _category;
    private String _action;
    private Date _date;

    public TimeMarker(String category, String action) {
        _category = category;
        _action = action;
        _date = new Date();
    }

    public String category() {
        return _category;
    }

    public String action() {
        return _action;
    }

    public String date() {
        final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
        return df.format(_date);
    }
}
