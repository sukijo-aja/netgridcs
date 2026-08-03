package com.androidstarter.app.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class AppAnalytics {

    private static FirebaseAnalytics mFirebaseAnalytics;
    private static FirebaseCrashlytics mCrashlytics;

    public static void init(Context context) {
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
        }
        if (mCrashlytics == null) {
            mCrashlytics = FirebaseCrashlytics.getInstance();
        }
    }

    public static void logEvent(String eventName, Bundle params) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(eventName, params);
        }
    }

    public static void logEvent(String eventName) {
        logEvent(eventName, null);
    }

    public static void logError(Exception e) {
        if (mCrashlytics != null) {
            mCrashlytics.recordException(e);
        }
    }

    public static void logError(String message) {
        if (mCrashlytics != null) {
            mCrashlytics.log(message);
        }
    }

    public static void setUserId(String userId) {
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserId(userId);
        }
        if (mCrashlytics != null) {
            mCrashlytics.setUserId(userId);
        }
    }

    /**
     * Start a custom Firebase Performance trace.
     * Call trace.stop() when the action finishes.
     */
    public static com.google.firebase.perf.metrics.Trace startTrace(String traceName) {
        com.google.firebase.perf.metrics.Trace trace = com.google.firebase.perf.FirebasePerformance.getInstance().newTrace(traceName);
        trace.start();
        return trace;
    }
}
