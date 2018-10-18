package com.adidas.hackathon.smartjacket.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.adidas.hackathon.smartjacket.R;


public class UIUtils {

    private static final int ALPHA_BIT_MASK = 0xFF000000;

    private UIUtils() {
    }

    /**
     * Returns a color android OS version independent way
     *
     * @param context    - context
     * @param colorResID - res id of the color
     * @return - extracted color
     */
    @ColorInt
    public static int getColor(Context context, @ColorRes int colorResID) {
        return ContextCompat.getColor(context, colorResID);
    }

    /**
     * Applies background drawable to View based upon android version
     *
     * @param drawable - Drawable to apply
     * @param view     - target for background
     */
    public static void setBackgroundDrawable(Drawable drawable, View view) {
        view.setBackground(drawable);
    }

    /**
     * Returns color (e.g. accent) color of the current theme attached to provided context (activity)
     *
     * @param context the activity we want to find the accent color for
     * @param attr    - attribute for what color
     * @return color result
     */
    @ColorInt
    public static int getAttrColor(Context context, int attr) {
        TypedArray a = obtainAttrTypedArray(context, attr);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    private static TypedArray obtainAttrTypedArray(Context c, int attr) {
        return c.obtainStyledAttributes(null, new int[]{attr});
    }

    /**
     * Returns the press effect color defined by current theme
     *
     * @param context context that defines the theme
     * @return the press effect color defined by current theme
     */
    @ColorInt
    public static int getThemePressEffectColor(Context context) {
        return getAttrColor(context, R.attr.colorControlHighlight);
    }

    /**
     * Returns true if color is transparent
     *
     * @param color color to check
     * @return is color transparent
     */
    public static boolean isColorTransparent(@ColorInt int color) {
        return (color & ALPHA_BIT_MASK) == 0;
    }

}
