package org.guytp.mscexperiment;

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
}
