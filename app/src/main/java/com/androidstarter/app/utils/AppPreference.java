package com.androidstarter.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

/**
 * Unified SharedPreferences wrapper with optional encryption.
 *
 * Regular usage (backward compatible):
 *   new AppPreference(context)
 *   new AppPreference(context, "profile_name")
 *
 * Encrypted usage (for sensitive data like tokens):
 *   AppPreference.encrypted(context)
 */
public class AppPreference {

    private static final String TAG = "AppPreference";
    private static final String DEFAULT_NAME = "config";
    private static final String ENCRYPTED_NAME = "secure_config";

    private final SharedPreferences sharedPreferences;

    /** Standard constructor — unencrypted. */
    public AppPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
    }

    /** Standard constructor with custom profile — unencrypted. */
    public AppPreference(Context context, String configProfile) {
        sharedPreferences = context.getSharedPreferences(configProfile, Context.MODE_PRIVATE);
    }

    /** Private constructor accepting any SharedPreferences instance. */
    private AppPreference(SharedPreferences prefs) {
        sharedPreferences = prefs;
    }

    /**
     * Factory method for encrypted SharedPreferences (AES-256).
     * Falls back to regular SharedPreferences if encryption fails.
     */
    public static AppPreference encrypted(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                    ENCRYPTED_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            return new AppPreference(encryptedPrefs);
        } catch (Exception e) {
            Log.e(TAG, "EncryptedSharedPreferences failed, falling back to regular", e);
            return new AppPreference(context.getApplicationContext());
        }
    }

    // ---- String ----
    public void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // ---- Int ----
    public void saveInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // ---- Float ----
    public void saveFloat(String key, float value) {
        sharedPreferences.edit().putFloat(key, value).apply();
    }

    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    // ---- Boolean ----
    public void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // ---- Long ----
    public void saveLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    // ---- Double (stored as raw long bits) ----
    public void saveDouble(String key, double value) {
        sharedPreferences.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
    }

    public double getDouble(String key, double defaultValue) {
        if (!sharedPreferences.contains(key)) return defaultValue;
        return Double.longBitsToDouble(sharedPreferences.getLong(key, Double.doubleToRawLongBits(defaultValue)));
    }

    // ---- Utility ----
    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean checkKey(String key) {
        return sharedPreferences.contains(key);
    }

    public java.util.Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}

