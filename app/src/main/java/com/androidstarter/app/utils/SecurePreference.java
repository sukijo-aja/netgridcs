package com.androidstarter.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Secure key-value storage using EncryptedSharedPreferences.
 * Use this for sensitive data: tokens, credentials, API keys, etc.
 * 
 * Usage:
 *   SecurePreference secure = SecurePreference.getInstance(context);
 *   secure.saveString("auth_token", "Bearer xyz...");
 *   String token = secure.getString("auth_token", null);
 */
public class SecurePreference {

    private static final String TAG = "SecurePreference";
    private static final String PREF_NAME = "secure_prefs";
    private static SecurePreference instance;
    private SharedPreferences prefs;

    private SecurePreference(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, falling back to regular", e);
            prefs = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    public static synchronized SecurePreference getInstance(Context context) {
        if (instance == null) {
            instance = new SecurePreference(context);
        }
        return instance;
    }

    public void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public boolean contains(String key) {
        return prefs.contains(key);
    }
}
