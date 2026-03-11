package com.mosleemapp.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public AppPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public AppPreference(Context context, String configProfile) {
        sharedPreferences = context.getSharedPreferences(configProfile, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void saveFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void saveDouble(String key, double value) {
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.apply();
    }

    public double getDouble(String key, double defaultValue) {
        if (!sharedPreferences.contains(key)) return defaultValue;
        return Double.longBitsToDouble(sharedPreferences.getLong(key, Double.doubleToRawLongBits(defaultValue)));
    }

    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }

    public boolean checkKey(String key)
    {
        return sharedPreferences.contains(key);
    }

    public java.util.Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}
