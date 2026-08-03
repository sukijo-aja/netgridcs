package com.androidstarter.app.utils;

import android.content.Context;
import android.content.Intent;

import com.androidstarter.app.ui.activities.LoginActivity;

/**
 * Centralized session lifecycle manager.
 * Stores tokens and metadata in EncryptedSharedPreferences.
 *
 * Usage:
 *   // After login success
 *   SessionManager.createSession(context, token, userId, 604800); // 7 days
 *
 *   // Check before protected actions
 *   if (!SessionManager.isSessionValid(context)) SessionManager.logout(context);
 *
 *   // Get token for API header
 *   String token = SessionManager.getToken(context);
 */
public class SessionManager {

    private static final String KEY_TOKEN          = "session_token";
    private static final String KEY_USER_ID        = "session_user_id";
    private static final String KEY_USER_EMAIL     = "session_email";
    private static final String KEY_DISPLAY_NAME   = "session_display_name";
    private static final String KEY_CREATED_AT     = "session_created_at";
    private static final String KEY_EXPIRES_AT     = "session_expires_at";
    private static final String KEY_LAST_ACTIVE_AT = "session_last_active";
    private static final long   IDLE_TIMEOUT_MS    = 30L * 24 * 60 * 60 * 1000; // 30 days

    private static AppPreference getPrefs(Context context) {
        return AppPreference.encrypted(context);
    }

    // ---- Create / Update ----

    /**
     * Call after successful login to persist session data.
     *
     * @param token          Bearer token from server.
     * @param userId         Unique user ID from server.
     * @param expiresInSecs  Token TTL in seconds (e.g. 604800 = 7 days).
     */
    public static void createSession(Context context, String token, String userId, long expiresInSecs) {
        long now = System.currentTimeMillis();
        AppPreference prefs = getPrefs(context);
        prefs.saveString(KEY_TOKEN, token);
        prefs.saveString(KEY_USER_ID, userId);
        prefs.saveLong(KEY_CREATED_AT, now);
        prefs.saveLong(KEY_EXPIRES_AT, now + (expiresInSecs * 1000));
        prefs.saveLong(KEY_LAST_ACTIVE_AT, now);
    }

    /** Save or update optional profile metadata associated with the session. */
    public static void updateProfile(Context context, String email, String displayName) {
        AppPreference prefs = getPrefs(context);
        prefs.saveString(KEY_USER_EMAIL, email);
        prefs.saveString(KEY_DISPLAY_NAME, displayName);
    }

    /** Refresh the token while keeping existing session metadata. */
    public static void refreshToken(Context context, String newToken, long newExpiresInSecs) {
        AppPreference prefs = getPrefs(context);
        prefs.saveString(KEY_TOKEN, newToken);
        prefs.saveLong(KEY_EXPIRES_AT, System.currentTimeMillis() + (newExpiresInSecs * 1000));
        touchActivity(context);
    }

    // ---- Read ----

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, null);
    }

    public static String getUserId(Context context) {
        return getPrefs(context).getString(KEY_USER_ID, null);
    }

    public static String getUserEmail(Context context) {
        return getPrefs(context).getString(KEY_USER_EMAIL, null);
    }

    public static String getDisplayName(Context context) {
        return getPrefs(context).getString(KEY_DISPLAY_NAME, null);
    }

    /** Returns true if a session exists, the token is not expired, and the user is not idle. */
    public static boolean isSessionValid(Context context) {
        AppPreference prefs = getPrefs(context);
        String token = prefs.getString(KEY_TOKEN, null);
        if (token == null || token.isEmpty()) return false;

        long now = System.currentTimeMillis();

        // Check token expiry
        long expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0);
        if (expiresAt > 0 && now > expiresAt) return false;

        // Check idle timeout
        long lastActive = prefs.getLong(KEY_LAST_ACTIVE_AT, 0);
        if (lastActive > 0 && (now - lastActive) > IDLE_TIMEOUT_MS) return false;

        return true;
    }

    public static boolean isLoggedIn(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, null) != null;
    }

    /** Returns remaining token TTL in milliseconds. 0 = expired. */
    public static long getTokenRemainingMs(Context context) {
        long expiresAt = getPrefs(context).getLong(KEY_EXPIRES_AT, 0);
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    // ---- Activity ----

    /** Call this on any user interaction to reset the idle timer. */
    public static void touchActivity(Context context) {
        getPrefs(context).saveLong(KEY_LAST_ACTIVE_AT, System.currentTimeMillis());
    }

    // ---- Logout ----

    /**
     * Clears all session data and redirects the user to LoginActivity.
     * Finishes all activities in the back stack.
     */
    public static void logout(Context context) {
        clearSession(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /** Clears session data without navigating away. Use this for token-only resets. */
    public static void clearSession(Context context) {
        AppPreference prefs = getPrefs(context);
        prefs.remove(KEY_TOKEN);
        prefs.remove(KEY_USER_ID);
        prefs.remove(KEY_USER_EMAIL);
        prefs.remove(KEY_DISPLAY_NAME);
        prefs.remove(KEY_CREATED_AT);
        prefs.remove(KEY_EXPIRES_AT);
        prefs.remove(KEY_LAST_ACTIVE_AT);
    }
}
