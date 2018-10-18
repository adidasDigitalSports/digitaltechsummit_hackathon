package com.adidas.hackathon.smartjacket.ui.ripple;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;

class RippleCreator {

    /**
     * Creates a ripple drawable.
     *
     * @param normalDrawable drawable for normal state
     * @param mask           ripple mask drawable. This should be used in cases where the normal/pressed drawable is transparent. If mask is not used on transparent drawables, it is not seen as after alpha composting, as the new color is still transparent.
     * @param rippleColor    color of the ripple
     */
    @SuppressLint("NewApi")
    static Drawable createRipple(Drawable normalDrawable, Drawable mask, int rippleColor) {
        ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{rippleColor});
        return createRipple(normalDrawable, mask, colorStateList);
    }

    /**
     * Creates a ripple with given color state list
     *
     * @param normalDrawable drawable for normal state
     * @param mask           ripple mask drawable. This should be used in cases where the normal/pressed drawable is transparent. If mask is not used on transparent drawables, it is not seen as after alpha composting, as the new color is still transparent.
     * @param colorStateList ripple color state list
     * @return ripple drawable that uses given color state list
     */
    @SuppressLint("NewApi")
    private static Drawable createRipple(Drawable normalDrawable, Drawable mask, ColorStateList colorStateList) {
        return new RippleDrawable(colorStateList, normalDrawable, mask);
    }

}
