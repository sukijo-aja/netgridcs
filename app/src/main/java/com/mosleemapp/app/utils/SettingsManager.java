package com.mosleemapp.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private static final String PREF_NAME = "MoslemAppPrefs";
    private static final String KEY_ARABIC_FONT_SIZE = "label_font_size";
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

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public float getArabicFontSize() {
        return sharedPreferences.getFloat(KEY_ARABIC_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public void saveArabicFontSize(float size) {
        sharedPreferences.edit().putFloat(KEY_ARABIC_FONT_SIZE, size).apply();
    }

    public boolean isReminderEnabled() {
        return sharedPreferences.getBoolean("prayer_reminder_enabled", false);
    }

    public void setReminderEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean("prayer_reminder_enabled", enabled).apply();
    }

    public boolean isPrayerAlarmEnabled(String prayerName) {
        return sharedPreferences.getBoolean("alarm_" + prayerName, true);
    }

    public void setPrayerAlarmEnabled(String prayerName, boolean enabled) {
        sharedPreferences.edit().putBoolean("alarm_" + prayerName, enabled).apply();
    }

    public int getPrayerAlarmOffset(String prayerName) {
        return sharedPreferences.getInt("offset_" + prayerName, 0);
    }

    public void setPrayerAlarmOffset(String prayerName, int minutes) {
        sharedPreferences.edit().putInt("offset_" + prayerName, minutes).apply();
    }
    public int getPrayerTimeCorrection(String prayerName) {
        return sharedPreferences.getInt("correction_" + prayerName, 0);
    }

    public void setPrayerTimeCorrection(String prayerName, int minutes) {
        sharedPreferences.edit().putInt("correction_" + prayerName, minutes).apply();
    }

    public int getPrePrayerReminderMinutes() {
        return sharedPreferences.getInt("pre_prayer_reminder_minutes", 0);
    }

    public void setPrePrayerReminderMinutes(int minutes) {
        sharedPreferences.edit().putInt("pre_prayer_reminder_minutes", minutes).apply();
    }

    public boolean isPremium() {
        return sharedPreferences.getBoolean("is_premium", false);
    }

    public void setPremium(boolean isPremium) {
        sharedPreferences.edit().putBoolean("is_premium", isPremium).apply();
    }

    public String getUserId() {
        String userId = sharedPreferences.getString("user_id", null);
        if (userId == null) {
            userId = java.util.UUID.randomUUID().toString();
            sharedPreferences.edit().putString("user_id", userId).apply();
        }
        return userId;
    }
}
