package com.mosleemapp.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREF_NAME = "MoslemAppPrefs";
    private static final String KEY_ARABIC_FONT_SIZE = "arabic_font_size";
    private static final float DEFAULT_FONT_SIZE = 24f; // Default matches XML

    private static SettingsManager instance;
    private SharedPreferences sharedPreferences;

    private SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    public float getArabicFontSize() {
        return sharedPreferences.getFloat(KEY_ARABIC_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public void saveArabicFontSize(float size) {
        sharedPreferences.edit().putFloat(KEY_ARABIC_FONT_SIZE, size).apply();
    }
}
