package com.adidas.hackathon.smartjacket.ui;

import android.support.annotation.IntDef;

@IntDef({SensorStatus.CONNECTED, SensorStatus.NOT_CONNECTED, SensorStatus.CONNECTING, SensorStatus.BLUETOOTH_OFF, SensorStatus.NO_BLUETOOTH_PERMISSION})
public @interface SensorStatus {
    int CONNECTED = 0;
    int NOT_CONNECTED = 1;
    int CONNECTING = 2;
    int BLUETOOTH_OFF = 3;
    int NO_BLUETOOTH_PERMISSION = 4;
}