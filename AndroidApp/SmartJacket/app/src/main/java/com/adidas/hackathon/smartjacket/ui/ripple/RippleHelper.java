package com.adidas.hackathon.smartjacket.ui.ripple;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;

import com.adidas.hackathon.smartjacket.ui.drawables.RoundRectangleDrawable;
import com.adidas.hackathon.smartjacket.util.UIUtils;

public class RippleHelper {

    private static final int TRANSPARENT = 0;
    private static final int NO_CORNERS = 0;
    private static final int NO_STROKE = 0;

    /**
     * Simplified version of apply ripple method.
     * The target for ripple will receive a rectangle version of drawable, which by default is transparent and changes when the state of the drawable changes.
     *
     * @param targets     - target views to apply ripple to.
     * @param rippleColor - color of the ripple
     */
    public static void applyRippleSimple(int rippleColor, View... targets) {
        // Each view must have it's own drawable instance, otherwise the press-effect may be displayed on the wrong view!
        for (View view : targets) {
            UIUtils.setBackgroundDrawable(createRippleSimple(rippleColor), view);
        }
    }

    /**
     * Creates a simple ripple drawable that is rectangle shaped with white mask color
     *
     * @param rippleColor - ripple color
     * @return - ripple
     */
    private static Drawable createRippleSimple(int rippleColor) {
        RoundRectangleDrawable normalState = new RoundRectangleDrawable(TRANSPARENT, TRANSPARENT, NO_STROKE, NO_CORNERS);
        RoundRectangleDrawable pressedDrawable = new RoundRectangleDrawable(rippleColor, TRANSPARENT, NO_STROKE, NO_CORNERS);
        RoundRectangleDrawable maskDrawable = new RoundRectangleDrawable(Color.WHITE, TRANSPARENT, NO_STROKE, NO_CORNERS);

        return createRippleDrawable(normalState, pressedDrawable, maskDrawable, rippleColor);
    }

    /**
     * Applies ripple effect to target.
     *
     * @param target          - target view to apply ripple to.
     * @param normalDrawable  - drawable for normal state
     * @param pressedDrawable - drawable for pressed state. This is used on pre-lolipop devices!
     * @param mask            - ripple mask drawable. This should be used in cases where the normal/pressed drawable is transparent. If mask is not used on transparent drawables, it is not seen as after alpha composting, as the new color is still transparent.
     * @param rippleColor     - color of the ripple
     */
    public static void applyRipple(Drawable normalDrawable, Drawable pressedDrawable, Drawable mask, int rippleColor, View target) {
        UIUtils.setBackgroundDrawable(createRippleDrawable(normalDrawable, pressedDrawable, mask, rippleColor), target);
    }

    /**
     * Creates a ripple drawable.
     *
     * @param normalDrawable  - drawable for normal state
     * @param pressedDrawable - drawable for pressed state. This is used on pre-lolipop devices!
     * @param mask            - ripple mask drawable. This should be used in cases where the normal/pressed drawable is transparent. If mask is not used on transparent drawables, it is not seen as after alpha composting, as the new color is still transparent.
     * @param rippleColor     - color of the ripple
     */
    private static Drawable createRippleDrawable(Drawable normalDrawable, Drawable pressedDrawable, Drawable mask, int rippleColor) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        boolean isRipplePossible = sdk >= Build.VERSION_CODES.LOLLIPOP;

        Drawable returnDrawable;

        if (isRipplePossible) {
            returnDrawable = RippleCreator.createRipple(normalDrawable, mask, rippleColor);
        } else {
            StateListDrawable stateList = new StateListDrawable();
            stateList.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateList.addState(new int[]{}, normalDrawable);
            returnDrawable = stateList;
        }
        return returnDrawable;
    }


}
