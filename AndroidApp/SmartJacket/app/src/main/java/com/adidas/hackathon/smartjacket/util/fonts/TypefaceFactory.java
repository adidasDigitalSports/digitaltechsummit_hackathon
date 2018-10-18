package com.adidas.hackathon.smartjacket.util.fonts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.adidas.hackathon.smartjacket.R;

public class TypefaceFactory {

    private static final String KEY_ROBOTO_REGULAR = "sans-serif";
    private static final String KEY_ROBOTO_BOLD = "sans-serif-bold";
    private static final String KEY_ROBOTO_MEDIUM = "sans-serif-medium";

    private static final int NON_VALID_VALUE = -123;

    /**
     * Initializes typeface component from the attribute set
     *
     * @param typefaceComponent component that we are initializing
     * @param context           context
     * @param attributeSet      attribute set for component
     * @param defStyleAttrs     def style attr for component
     */
    public static void initializeTypefaceComponent(TypefaceComponent typefaceComponent, Context context, AttributeSet attributeSet, int defStyleAttrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TypefaceComponent, defStyleAttrs, 0);
        int typefaceValue = typedArray.getInt(R.styleable.TypefaceComponent_tcTypeface, NON_VALID_VALUE);
        typedArray.recycle();
        if (typefaceValue != NON_VALID_VALUE) {
            // noinspection WrongConstant
            Typeface t = typefaceValueToTypeface(context, typefaceValue);
            if (t != null) {
                typefaceComponent.applyTypeface(t);
            }
        }
    }

    /**
     * Converts typeface value to typeface
     *
     * @param context context
     * @param value   value for the typeface
     * @return typeface for given typeface value
     */
    public static Typeface typefaceValueToTypeface(Context context, @TypefaceValues int value) {
        Typeface ret = null;
        switch (value) {
            case TypefaceValues.ADI_NEUE_PROTT_REGULAR:
                ret = TypefaceCache.get(context, "adineuePROTT-Regular");
                break;
            case TypefaceValues.ADI_NEUE_PROTT_BOLD:
                ret = TypefaceCache.get(context, "adineuePROTT-Bold");
                break;
            case TypefaceValues.ADI_NEUE_PROTT_LIGHT:
                ret = TypefaceCache.get(context, "adineuePROTT-Light");
                break;
            case TypefaceValues.ADI_NEUE_PROTT_BLACK:
                ret = TypefaceCache.get(context, "adineuePROTT-Black");
                break;
            case TypefaceValues.ADI_HAUS_DIN_BOLD:
                ret = TypefaceCache.get(context, "AdihausDIN-Bold");
                break;
            case TypefaceValues.ADI_HAUS_DIN_REGULAR:
                ret = TypefaceCache.get(context, "AdihausDIN-Regular");
                break;
            case TypefaceValues.ROBOTO_REGULAR:
                ret = getOrCreate(KEY_ROBOTO_REGULAR, "sans-serif", Typeface.NORMAL);
                break;
            case TypefaceValues.ROBOTO_BOLD:
                ret = getOrCreate(KEY_ROBOTO_BOLD, "sans-serif", Typeface.BOLD);
                break;
            case TypefaceValues.ROBOTO_MEDIUM:
                // Roboto medium is natively supported since lollipop, older version will read the font from assets
                ret = getOrCreate(KEY_ROBOTO_MEDIUM, "sans-serif-medium", Typeface.NORMAL);
                break;
        }
        return ret;
    }

    private static Typeface getOrCreate(String cacheKey, String typefaceName, int typefaceStyle) {
        Typeface ret = TypefaceCache.getDontCreate(cacheKey);
        if (ret == null) {
            ret = Typeface.create(typefaceName, typefaceStyle);
            TypefaceCache.put(ret, cacheKey);
        }
        return ret;
    }

}
