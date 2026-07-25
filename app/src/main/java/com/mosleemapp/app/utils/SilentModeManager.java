package com.mosleemapp.app.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.mosleemapp.app.utils.AppPreference;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * Manages automatic silent mode activation/restoration during prayer times.
 * Saves the previous ringer mode before silencing and restores it afterwards.
 */
public class SilentModeManager {

    private static final String TAG = "SilentModeManager";
    private static final String PREF_NAME = "SilentModePrefs";
    private static final String KEY_PREVIOUS_RINGER_MODE = "previous_ringer_mode";
    private static final String KEY_IS_SILENCED_BY_APP = "is_silenced_by_app";

    /**
     * Activate silent (vibrate) mode, saving the current ringer mode first.
     */
    public static void activateSilentMode(Context context) {
        if (!hasDoNotDisturbPermission(context)) {
            Log.w(TAG, "No DND permission, cannot activate silent mode");
            return;
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        AppPreference prefs = new AppPreference(context, PREF_NAME);

        // Only save previous mode if we haven't already silenced (avoid overwriting stored mode)
        boolean alreadySilenced = prefs.getBoolean(KEY_IS_SILENCED_BY_APP, false);
        if (!alreadySilenced) {
            int currentMode = audioManager.getRingerMode();
            prefs.saveInt(KEY_PREVIOUS_RINGER_MODE, currentMode);
            prefs.saveBoolean(KEY_IS_SILENCED_BY_APP, true);
            Log.d(TAG, "Saved previous ringer mode: " + currentMode);
        }

        // Set to vibrate mode
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        Log.d(TAG, "Silent mode activated (vibrate)");
    }

    /**
     * Restore the ringer mode that was saved before silent was activated.
     */
    public static void restoreRingerMode(Context context) {
        if (!hasDoNotDisturbPermission(context)) {
            Log.w(TAG, "No DND permission, cannot restore ringer mode");
            return;
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        AppPreference prefs = new AppPreference(context, PREF_NAME);
        boolean wasSilencedByApp = prefs.getBoolean(KEY_IS_SILENCED_BY_APP, false);

        if (wasSilencedByApp) {
            int previousMode = prefs.getInt(KEY_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL);
            audioManager.setRingerMode(previousMode);
            prefs.saveBoolean(KEY_IS_SILENCED_BY_APP, false);
            Log.d(TAG, "Ringer mode restored to: " + previousMode);
        } else {
            Log.d(TAG, "Ringer was not silenced by app, no restore needed");
        }
    }

    /**
     * Check if the app has Do Not Disturb / notification policy access.
     * Required on Android M (API 23)+ to modify ringer mode.
     */
    public static boolean hasDoNotDisturbPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return nm != null && nm.isNotificationPolicyAccessGranted();
        }
        // Below API 23, no special permission needed
        return true;
    }

    /**
     * Open the Do Not Disturb access settings so user can grant permission.
     */
    public static void requestDoNotDisturbPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
