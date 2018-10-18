package com.adidas.hackathon.smartjacket.util;

import android.app.Activity;
import android.content.Context;

import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * A helper class for requesting permissions.
 */
public class PermissionsHandler {

    private final RxPermissions rxPermissions;

    public PermissionsHandler(Context context) {
        rxPermissions = new RxPermissions((Activity) context);
    }

    public RxPermissions getRxPermissions() {
        return rxPermissions;
    }

}
