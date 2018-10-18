package com.adidas.hackathon.smartjacket.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.adidas.hackathon.smartjacket.util.fonts.CustomTypefaceSpan;
import com.adidas.hackathon.smartjacket.util.fonts.TypefaceFactory;
import com.adidas.hackathon.smartjacket.util.fonts.TypefaceValues;

public class Tools {

    /**
     * Query if the device (phone) has BLE-compatible hardware.
     */
    public static boolean hasBLE(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Query if the device's BLE hardware is enabled. If not request user to enable it.
     */
    public static void checkBluetoothEnabled(Context context) {
        if (!Tools.isBluetoothEnabled()) {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    private static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Finds the &lt;b> and &lt;/b> markings in the text and sets the typeface adineuePROTT-Bold to the
     * text in-between.
     */
    public static Spannable getCustomSpannableString(Context context, CharSequence text) {
        SpannableString ss = new SpannableString(text);
        Typeface typeface = TypefaceFactory.typefaceValueToTypeface(context, TypefaceValues.ADI_NEUE_PROTT_BOLD);

        for (StyleSpan span : ss.getSpans(0, ss.length(), StyleSpan.class)) {
            if (span.getStyle() == Typeface.BOLD) {
                int start = ss.getSpanStart(span);
                int end = ss.getSpanEnd(span);

                ss.removeSpan(span);
                ss.setSpan(new CustomTypefaceSpan(typeface), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ss;
    }

}
