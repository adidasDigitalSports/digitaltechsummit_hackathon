package com.adidas.hackathon.smartjacket.util.fonts;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;
import java.util.Locale;

public class TypefaceCache {
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    static Typeface get(Context context, String name) {
        synchronized (cache) {
            if (!cache.containsKey(name)) {
                Typeface t = Typeface
                        .createFromAsset(context.getAssets(), String.format(Locale.ENGLISH, "fonts/%s.ttf", name));
                cache.put(name, t);
            }
            return cache.get(name);
        }
    }

    static Typeface getDontCreate(String name) {
        synchronized (cache) {
            return cache.get(name);
        }
    }

    public static void put(Typeface typeface, String key) {
        synchronized (cache) {
            cache.put(key, typeface);
        }
    }
}
