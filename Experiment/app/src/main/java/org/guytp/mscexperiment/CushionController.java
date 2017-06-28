package org.guytp.mscexperiment;


public class CushionController {
    private static CushionController _applicationInstance = new CushionController();

    private CushionController() {
        // TODO: Bluetooth management
    }

    public static CushionController getInstance() {
        return _applicationInstance;
    }

    public void setState(CushionState state) {
        // TODO: Actual implementation here
    }

    public void off() {
        // TODO: Actual implementation here - switch of light/vibration
    }

}