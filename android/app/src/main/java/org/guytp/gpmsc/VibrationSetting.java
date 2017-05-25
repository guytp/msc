package org.guytp.gpmsc;

/**
 * Created by guytp on 25/05/17.
 */

public class VibrationSetting {
    public VibrationSettingValue[] values;
    public byte numberValues;

    public VibrationSetting() {
        values = new VibrationSettingValue[5];
        for (int j = 0; j < 5; j++)
            values[j] = new VibrationSettingValue();
    }
}
