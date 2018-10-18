package com.adidas.hackathon.smartjacket.util.fonts;

import android.support.annotation.IntDef;

@IntDef({TypefaceValues.ROBOTO_REGULAR, TypefaceValues.ROBOTO_BOLD, TypefaceValues.ROBOTO_MEDIUM,
        TypefaceValues.ADI_NEUE_PROTT_BOLD, TypefaceValues.ADI_NEUE_PROTT_REGULAR,
        TypefaceValues.ADI_NEUE_PROTT_LIGHT, TypefaceValues.ADI_NEUE_PROTT_BLACK,
        TypefaceValues.ADI_HAUS_DIN_BOLD, TypefaceValues.ADI_HAUS_DIN_REGULAR})
public @interface TypefaceValues {
    int ROBOTO_REGULAR = 0;
    int ROBOTO_BOLD = 1;
    int ROBOTO_MEDIUM = 2;

    int ADI_NEUE_PROTT_REGULAR = 3;
    int ADI_NEUE_PROTT_BOLD = 4;
    int ADI_NEUE_PROTT_LIGHT = 5;
    int ADI_NEUE_PROTT_BLACK = 6;
    int ADI_HAUS_DIN_BOLD = 7;
    int ADI_HAUS_DIN_REGULAR = 8;
}
