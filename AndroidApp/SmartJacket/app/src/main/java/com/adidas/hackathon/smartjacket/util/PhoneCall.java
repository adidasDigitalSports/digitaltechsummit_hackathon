package com.adidas.hackathon.smartjacket.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class PhoneCall {

    public static final String TAG = PhoneCall.class.getName();

    // Minimum time between subsequent calls, and the time at which the last call was placed.
    // These are used to prevent "spamming" phone calls from the app.
    private static final float MIN_TIME_BETWEEN_CALLS = 2000;
    private static float timeLastCall;

    /**
     * Places a phone call to the specified number.
     */
    public static void startCall(String number, Context context) {
        if (System.currentTimeMillis() - timeLastCall > MIN_TIME_BETWEEN_CALLS) {
            timeLastCall = System.currentTimeMillis();
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                context.startActivity(callIntent);
            } catch (Exception e) {
                Log.i(TAG, "startCall() ERROR: Call failed, system error message follows...");
            }
        }
    }

}
