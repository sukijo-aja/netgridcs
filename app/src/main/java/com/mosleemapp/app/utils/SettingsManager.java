package com.mosleemapp.app.utils;

import android.content.Context;

import com.google.firebase.installations.FirebaseInstallations;


public class SettingsManager {
    private static final String PREF_NAME = "config";
    private static final String KEY_ARABIC_FONT_SIZE = "label_font_size";
    private static final float DEFAULT_FONT_SIZE = 24f; // Default matches XML

    private static SettingsManager instance;
    private AppPreference appPreference;

    private SettingsManager(Context context) {
        appPreference = new AppPreference(context, PREF_NAME);
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    public AppPreference getAppPreference() {
        return appPreference;
    }

    public float getArabicFontSize() {
        return appPreference.getFloat(KEY_ARABIC_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public void saveArabicFontSize(float size) {
        appPreference.saveFloat(KEY_ARABIC_FONT_SIZE, size);
    }

    public boolean isTajweedEnabled() {
        return appPreference.getBoolean("show_tajweed", false);
    }

    public void setTajweedEnabled(boolean enabled) {
        appPreference.saveBoolean("show_tajweed", enabled);
    }

    public boolean isReminderEnabled() {
        return appPreference.getBoolean("prayer_reminder_enabled", false);
    }

    public void setReminderEnabled(boolean enabled) {
        appPreference.saveBoolean("prayer_reminder_enabled", enabled);
    }

    public boolean isPrayerAlarmEnabled(String prayerName) {
        return appPreference.getBoolean("alarm_" + prayerName, true);
    }

    public void setPrayerAlarmEnabled(String prayerName, boolean enabled) {
        appPreference.saveBoolean("alarm_" + prayerName, enabled);
    }

    public int getPrayerAlarmOffset(String prayerName) {
        return appPreference.getInt("offset_" + prayerName, 0);
    }

    public void setPrayerAlarmOffset(String prayerName, int minutes) {
        appPreference.saveInt("offset_" + prayerName, minutes);
    }
    public int getPrayerTimeCorrection(String prayerName) {
        return appPreference.getInt("correction_" + prayerName, 0);
    }

    public void setPrayerTimeCorrection(String prayerName, int minutes) {
        appPreference.saveInt("correction_" + prayerName, minutes);
    }

    public int getPrePrayerReminderMinutes() {
        return appPreference.getInt("pre_prayer_reminder_minutes", 0);
    }

    public void setPrePrayerReminderMinutes(int minutes) {
        appPreference.saveInt("pre_prayer_reminder_minutes", minutes);
    }

    // --- Auto Silent Mode ---

    public boolean isAutoSilentEnabled() {
        return appPreference.getBoolean("auto_silent_enabled", false);
    }

    public void setAutoSilentEnabled(boolean enabled) {
        appPreference.saveBoolean("auto_silent_enabled", enabled);
    }

    public int getAutoSilentDuration() {
        return appPreference.getInt("auto_silent_duration", 15); // default 15 minutes
    }

    public void setAutoSilentDuration(int minutes) {
        appPreference.saveInt("auto_silent_duration", minutes);
    }

    public boolean isPremium() {
        return appPreference.getBoolean("is_premium", false);
    }

    public void setPremium(boolean isPremium) {
        appPreference.saveBoolean("is_premium", isPremium);
    }

    public String getUserId() {
        String userId = appPreference.getString("user_id", null);
        if (userId == null) {
            // Trigger fetch but return a temporary placeholder or wait
            // ideally we return what we have. If null, we fetch.
            fetchFirebaseId();
            return "Fetching...";
        }
        return userId;
    }

    public void fetchFirebaseId() {
        FirebaseInstallations.getInstance().getId()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String fid = task.getResult();
                    appPreference.saveString("user_id", fid);
                } else {
                    // Fallback to UUID
                    String uuid = java.util.UUID.randomUUID().toString();
                    appPreference.saveString("user_id", uuid);
                }
            });
    }

    public int getCalculationMethod() {
        return appPreference.getInt("calculation_method", 20); // Default to MUIS
    }

    public void setCalculationMethod(int method) {
        appPreference.saveInt("calculation_method", method);
    }
}

