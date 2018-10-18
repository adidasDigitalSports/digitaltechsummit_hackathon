package com.adidas.hackathon.smartjacket.ble;


import java.util.ArrayList;

public interface BleInterface {

    // default intervals for scanning BLE devices
    // and updating the device (jacket) stats once connected
    int DEVICE_SCAN_INTERVAL   = 6000;
    int DEVICE_UPDATE_INTERVAL = 800;

    // current training plan state on the device
    int PLAN_STATE_NONE    = 0;
    int PLAN_STATE_READY   = 1;
    int PLAN_STATE_STARTED = 2;
    int PLAN_STATE_PAUSED  = 3;
    int PLAN_STATE_ENDED   = 4;

    // tactile control modes
    int CONTROL_NONE  = 0;
    int CONTROL_MUSIC = 1;
    int CONTROL_CALLS = 2;

    /** Initialize the object's BLE logic. */
    void init();
    /** Dispose of the object. */
    void dispose();
    /** Check if the object has been initialized. */
    boolean isInitialized();


    /** If the object is currently scanning for BLE devices. */
    boolean isScanning();

    /**
     * Start scanning for BLE devices, until the interval elapses (milliseconds).
     * Results are handled via the parent's callback. See specific implementation.
     */
    int startScanning(int scanInterval);

    /** Stop scanning for BLE devices. */
    int stopScanning();

    /** Returns the number of named BLE devices found in the last scan. */
    int getResultCount();

    /** Get the list of named BLE devices found in the last scan, as pairs of name and MAC Address. */
    ArrayList<BleDeviceSimple> getDeviceList();

    /** Get a specific item from the list of named BLE devices found in the last scan, as a pair of name and MAC Address. */
    BleDeviceSimple getScanResult(int i);

    /**
     * Attempt a connection to a device identified by MAC address.
     * The result is handled via the parent's callback. See specific implementation.
     */
    int connectTo(String deviceAddress);
    /**
     * Attempt a connection to a device represented by a BLEDeviceSimple object.
     * The result is handled via the parent's callback. See specific implementation.
     */
    int connectTo(BleDeviceSimple device);
    /**
     * Disconnect from the currently connected BLE device.
     * The result is handled via the parent's callback. See specific implementation.
     */
    int disconnect();

    /** Whether the object is currently trying to connect to a BLE device. */
    boolean isConnecting();

    /** Whether the object is currently connected to a BLE device. */
    boolean isConnected();

    /** Whether the object is currently connected and ready to be used (i.e., not discovering or configuring services). */
    boolean isReady();

    /** Get the name and MAC address of the currently connected device (or null if not connected). */
    BleDeviceSimple getDevice();


    /** Read the server (jacket) state, by reading and parsing the appropriate characteristics. */
    int updateState();

    /** Send a plan to the server (jacket) as a JSON-formatted string. */
    int sendPlan(String jsonStringPlan);

    /** Set the jacket's tactile control to calls. */
    int setControlCalls();

    /** Set the jacket's tactile control to music. */
    int setControlMusic();



    /** Get the name of the current plan on the jacket (since the last update). */
    String getDevicePlanName();

    /** Get the state of the training plan on the jacket (since the last update). */
    int getDevicePlanState();

    /** Get the current phase in the training plan on the jacket (since the last update). */
    int getDeviceCurrPhase();

    /** Get the current progress within a training phase on the jacket (since the last update). */
    int getDeviceProgress();

    /** Get the current tactile control mode (since the last update). */

    int getDeviceCtrlMode();

    /** Get the current battery value (since the last update). */
    int getDeviceBattery();

}
