package com.androidstarter.app.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class FirebaseUtil {

    private static FirebaseUtil instance;
    private final FirebaseAnalytics firebaseAnalytics;
    private final FirebaseRemoteConfig firebaseRemoteConfig;

    private FirebaseUtil(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
    }

    public static synchronized FirebaseUtil getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseUtil(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Log a custom event to Firebase Analytics
     */
    public void logEvent(String eventName, Bundle params) {
        firebaseAnalytics.logEvent(eventName, params);
    }

    /**
     * Set a user property for Firebase Analytics
     */
    public void setUserProperty(String name, String value) {
        firebaseAnalytics.setUserProperty(name, value);
    }

    /**
     * Fetch and activate Remote Config values
     */
    public void fetchRemoteConfig() {
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Updated values are now active
                    }
                });
    }

    /**
     * Get a String value from Remote Config
     */
    public String getString(String key) {
        return firebaseRemoteConfig.getString(key);
    }

    /**
     * Get a boolean value from Remote Config
     */
    public boolean getBoolean(String key) {
        return firebaseRemoteConfig.getBoolean(key);
    }

    /**
     * Get a long value from Remote Config
     */
    public long getLong(String key) {
        return firebaseRemoteConfig.getLong(key);
    }
    
    /**
     * Get Firebase Installation ID
     */
    public void getInstallationId(InstallationIdCallback callback) {
        FirebaseInstallations.getInstance().getId()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                   callback.onIdReceived(task.getResult());
                } else {
                   callback.onIdReceived(null);
                }
            });
    }
    
    /**
     * Get Firebase Cloud Messaging Token
     */
    public void getFcmToken(TokenCallback callback) {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onTokenReceived(task.getResult());
                } else {
                    callback.onTokenReceived(null);
                }
            });
    }

    public interface InstallationIdCallback {
        void onIdReceived(String id);
    }

    public interface TokenCallback {
        void onTokenReceived(String token);
    }
}
