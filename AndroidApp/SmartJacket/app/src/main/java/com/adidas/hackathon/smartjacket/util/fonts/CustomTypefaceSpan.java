package com.adidas.hackathon.smartjacket.util.fonts;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomTypefaceSpan extends MetricAffectingSpan {

    private final Typeface typeface;

    public CustomTypefaceSpan(final Typeface typeface) {
        this.typeface = typeface;
    }

    @Override
    public void updateDrawState(final TextPaint drawState) {
        apply(drawState);
    }

    @Override
    public void updateMeasureState(final TextPaint paint) {
        apply(paint);
    }

    // Usually when you make a font bold on your computer, there is a separate bold version of the font in question,
    // and that is used in place of the regular font. However, for that to work you have to know whether the font you're using
    // is already bold and what the corresponding fonts are. As a work around Android provides "fake" bold and italic modes,
    // which just applies some transforms to the regular font in order to make it appear bold or italic
    // so that you don't need to load multiple typeface).
    private void apply(final Paint paint) {
        final Typeface oldTypeface = paint.getTypeface();
        final int oldStyle = oldTypeface != null ? oldTypeface.getStyle() : 0;
        final int fakeStyle = oldStyle & ~typeface.getStyle();

        if ((fakeStyle & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fakeStyle & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(typeface);
    }

}
