package com.adidas.hackathon.smartjacket.ui.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.adidas.hackathon.smartjacket.R;
import com.adidas.hackathon.smartjacket.util.fonts.TypefaceComponent;
import com.adidas.hackathon.smartjacket.util.fonts.TypefaceFactory;

public class AdidasTextView extends AppCompatTextView implements TypefaceComponent {

    public AdidasTextView(Context context) {
        this(context, null);
    }

    public AdidasTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdidasTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyleAttr) {
        TypefaceFactory.initializeTypefaceComponent(this, context, attributeSet, defStyleAttr);

        TypedArray attributeArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.AdidasTextView);

        Drawable drawableLeft = attributeArray.getDrawable(R.styleable.AdidasTextView_drawableLeftCompat);
        Drawable drawableRight = attributeArray.getDrawable(R.styleable.AdidasTextView_drawableRightCompat);
        Drawable drawableBottom = attributeArray.getDrawable(R.styleable.AdidasTextView_drawableBottomCompat);
        Drawable drawableTop = attributeArray.getDrawable(R.styleable.AdidasTextView_drawableTopCompat);
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        attributeArray.recycle();
    }

    @Override
    public void applyTypeface(@NonNull Typeface typeface) {
        setTypeface(typeface);
    }

}
