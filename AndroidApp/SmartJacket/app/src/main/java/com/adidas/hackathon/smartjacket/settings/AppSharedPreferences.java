package com.adidas.hackathon.smartjacket.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences {

    private static final String PREF_NAME = "com.adidas.hackathon.PREFERENCE_FILE_KEY";

    private static final String BLE_DEVICE_NAME = "BleDeviceName";
    private static final String BLE_DEVICE_MAC = "BleDeviceMac";
    private static final String FAVORITE_CONTACT = "FavoriteContactName";
    private static final String FAVORITE_CONTACT_NUMBER = "FavoriteContactNumber";
    private static final String TRAINING_PLAN = "TrainingPlan";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static void putString(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static void saveBleDeviceName(Context context, String bleDeviceName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BLE_DEVICE_NAME, bleDeviceName);
        editor.apply();
    }

    public static String getBleDeviceName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(BLE_DEVICE_NAME, "NAME");
    }

    public static void saveBleDeviceMac(Context context, String bleDeviceMac) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BLE_DEVICE_MAC, bleDeviceMac);
        editor.apply();
    }

    public static String getBleDeviceMac(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(BLE_DEVICE_MAC, "MAC");
    }

    public static void savePrefContactName(Context context, String prefContact) {
        putString(context, FAVORITE_CONTACT, prefContact);
    }

    public static String getPrefContactName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(FAVORITE_CONTACT, " ");
    }

    public static void savePrefContactNumber(Context context, String prefContactNumber) {
        putString(context, FAVORITE_CONTACT_NUMBER, prefContactNumber);
    }

    public static String getPrefContactNumber(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(FAVORITE_CONTACT_NUMBER, " ");
    }

    public static void saveTrainingPlan(Context context, String jsonTrainingPlan) {
        putString(context, TRAINING_PLAN, jsonTrainingPlan);
    }

    public static String getTrainingPlan(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TRAINING_PLAN, " ");
    }


}

