package com.adidas.hackathon.smartjacket.ble;

/**
 * A simple representation of a BLE device, consisting of:
 * the device's name, which is more humanly readable;
 * the device's MAC address, which acts as unique ID.
 */
public class BleDeviceSimple {

    private String name;
    private String mac;
    private boolean isConnected;
    private boolean isSelected;

    BleDeviceSimple() {
        this.name = "unnamed";
        this.mac = "none";
    }

    public BleDeviceSimple(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    public String name() {
        return this.name;
    }

    public String mac() {
        return this.mac;
    }

    public boolean connected() {
        return this.isConnected;
    }

    public boolean selected() {
        return this.isSelected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMac(String address) {
        this.mac = address;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

}
