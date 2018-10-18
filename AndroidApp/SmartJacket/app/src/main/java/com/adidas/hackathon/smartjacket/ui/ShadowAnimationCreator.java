package com.adidas.hackathon.smartjacket.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.content.res.Resources;
import android.view.View;

import com.adidas.hackathon.smartjacket.R;

public class ShadowAnimationCreator {

    private static final int MATERIAL_ANIMATION_DURATION = 300;
    private static final int NON_VALID_DIMEN = -1;

    // Cache the values into these variables, so we are not reading them from the resources every time
    private static int BUTTON_PRESSED_ELEVATION = NON_VALID_DIMEN;
    private static int BUTTON_DEFAULT_ELEVATION = NON_VALID_DIMEN;

    private ShadowAnimationCreator() {
    }

    /**
     * Based on official sources: https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/anim/button_state_list_anim_material.xml
     */
    private static void createShadowAnimation(View target, int defaultElevation, int pressedElevation) {
        initDimensions(target);

        StateListAnimator animator = new StateListAnimator();

        AnimatorSet pressedSet = new AnimatorSet();
        ObjectAnimator pressedTranslation = ObjectAnimator.ofFloat(target, "translationZ", pressedElevation);
        pressedTranslation.setDuration(MATERIAL_ANIMATION_DURATION);
        ObjectAnimator pressedElevationAnimator = ObjectAnimator.ofFloat(target, "elevation", defaultElevation);
        pressedElevationAnimator.setDuration(0);
        pressedSet.play(pressedTranslation).with(pressedElevationAnimator);

        AnimatorSet enabledSet = new AnimatorSet();
        ObjectAnimator enabledTranslation = ObjectAnimator.ofFloat(target, "translationZ", 0);
        enabledTranslation.setDuration(MATERIAL_ANIMATION_DURATION);
        ObjectAnimator enabledElevation = ObjectAnimator.ofFloat(target, "elevation", defaultElevation);
        enabledElevation.setDuration(0);
        enabledSet.play(enabledTranslation).with(enabledElevation);

        AnimatorSet disabledSet = new AnimatorSet();
        ObjectAnimator disabledTranslation = ObjectAnimator.ofFloat(target, "translationZ", 0);
        enabledTranslation.setDuration(0);
        ObjectAnimator disabledElevation = ObjectAnimator.ofFloat(target, "elevation", 0);
        disabledElevation.setDuration(0);
        disabledSet.play(disabledTranslation).with(disabledElevation);

        animator.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, pressedSet);
        animator.addState(new int[]{android.R.attr.state_enabled}, enabledSet);
        animator.addState(new int[]{}, disabledSet);

        target.setStateListAnimator(animator);
        animator.jumpToCurrentState();
    }

    public static void createShadowAnimation(View target) {
        initDimensions(target);
        createShadowAnimation(target, BUTTON_DEFAULT_ELEVATION, BUTTON_PRESSED_ELEVATION);
    }

    private static void initDimensions(View view) {
        if (BUTTON_DEFAULT_ELEVATION == NON_VALID_DIMEN) {
            Resources resources = view.getResources();
            BUTTON_DEFAULT_ELEVATION = resources.getDimensionPixelSize(R.dimen.button_elevation_material);
            BUTTON_PRESSED_ELEVATION = resources.getDimensionPixelSize(R.dimen.button_pressed_z_material);
        }
    }

}
