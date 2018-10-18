package com.adidas.hackathon.smartjacket.ui.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

import com.adidas.hackathon.smartjacket.util.Tools;
import com.adidas.hackathon.smartjacket.util.fonts.TypefaceComponent;
import com.adidas.hackathon.smartjacket.util.fonts.TypefaceFactory;

public class AdidasHtmlTextView extends AdidasTextView implements TypefaceComponent {

    public AdidasHtmlTextView(Context context) {
        this(context, null);
    }

    public AdidasHtmlTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdidasHtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyleAttr) {
        TypefaceFactory.initializeTypefaceComponent(this, context, attributeSet, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(Tools.getCustomSpannableString(getContext(), text), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void applyTypeface(@NonNull Typeface typeface) {
        setTypeface(typeface);
    }

}
