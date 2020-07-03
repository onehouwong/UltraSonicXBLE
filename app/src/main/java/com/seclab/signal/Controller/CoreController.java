package com.seclab.signal.Controller;

public class CoreController {

    public BLEController bleController;
    public SonarController sonarController;

    public CoreController (BLEController b, SonarController s) {
        bleController = b;
        sonarController = s;
    }

    public void start() {
        switch (bleController.role) {
            case CENTRAL:

                break;

            case PERIPHERAL:

                break;
        }
    }
}
