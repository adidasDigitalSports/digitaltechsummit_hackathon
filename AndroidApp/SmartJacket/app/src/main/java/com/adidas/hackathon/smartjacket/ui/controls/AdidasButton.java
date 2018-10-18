package com.adidas.hackathon.smartjacket.ui.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.ui.ShadowAnimationCreator;
import com.adidas.hackathon.smartjacket.ui.drawables.RoundRectangleDrawable;
import com.adidas.hackathon.smartjacket.ui.ripple.RippleHelper;
import com.adidas.hackathon.smartjacket.util.UIUtils;

public class AdidasButton extends AdidasTextView {

    public AdidasButton(Context context) {
        this(context, null);
    }

    public AdidasButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdidasButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyleAttr) {
        initButton(this, context, attributeSet, defStyleAttr);
    }

    static void initButton(TextView textView, Context context, AttributeSet attributeSet, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.AdidasButton, defStyleAttr, 0);
        boolean shadowEnabled = ta.getBoolean(R.styleable.AdidasButton_abShadowEnabled, true);
        int normalColor = ta.getColor(R.styleable.AdidasButton_abFillColor, UIUtils.getAttrColor(context, R.attr.colorAccent));
        int pressedColor = ta.getColor(R.styleable.AdidasButton_abPressEffectColor, UIUtils.getThemePressEffectColor(context));
        int maskColor = ta.getColor(R.styleable.AdidasButton_abMaskColor, Color.TRANSPARENT);
        int strokeColor = ta.getColor(R.styleable.AdidasButton_abStrokeColor, Color.TRANSPARENT);
        float cornerRadius = ta.getDimension(R.styleable.AdidasButton_abCornerRadius, 0);
        float strokeWidth = ta.getDimension(R.styleable.AdidasButton_abStrokeWidth, 0);
        ta.recycle();

        if (shadowEnabled) {
            ShadowAnimationCreator.createShadowAnimation(textView);
        }

        RoundRectangleDrawable normalDrawable = new RoundRectangleDrawable(normalColor, strokeColor, strokeWidth, cornerRadius);
        RoundRectangleDrawable maskDrawable = null;

        if (!UIUtils.isColorTransparent(maskColor)) {
            maskDrawable = new RoundRectangleDrawable(maskColor, strokeColor, strokeWidth, cornerRadius);
        }

        RippleHelper.applyRipple(normalDrawable, null, maskDrawable, pressedColor, textView);
    }


}
