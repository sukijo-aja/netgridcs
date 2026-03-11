package com.mosleemapp.app.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.mosleemapp.app.R;
import com.mosleemapp.app.ui.activities.ReminderActivity;
import com.mosleemapp.app.utils.AlarmScheduler;
import com.mosleemapp.app.utils.SettingsManager;
import com.mosleemapp.app.utils.SilentModeManager;

public class PrayerAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "prayer_reminder_channel";
    private static final String CHANNEL_NAME = "Prayer Reminders";

    public static final String ACTION_RESTORE_RINGER = "com.mosleemapp.app.ACTION_RESTORE_RINGER";

    private String getLocalizedPrayerName(Context context, String prayerName) {
        if (prayerName == null) return "Prayer";
        switch (prayerName.toLowerCase()) {
            case "fajr": return context.getString(R.string.fajr);
            case "dhuhr": return context.getString(R.string.dhuhr);
            case "asr": return context.getString(R.string.asr);
            case "maghrib": return context.getString(R.string.maghrib);
            case "isha": return context.getString(R.string.isha);
            default: return prayerName;
        }
    }

    private String getNextFromCurrent(String current) {
        if (current == null) return "Unknown";
        switch (current.toLowerCase()) {
            case "fajr": return "Dhuhr";
            case "dhuhr": return "Asr";
            case "asr": return "Maghrib";
            case "maghrib": return "Isha";
            case "isha": return "Fajr";
            default: return "Unknown";
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle restore-ringer alarm
        if (ACTION_RESTORE_RINGER.equals(intent.getAction())) {
            SilentModeManager.restoreRingerMode(context);
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SettingsManager sm = SettingsManager.getInstance(context);
            if (sm.isReminderEnabled()) {
                AlarmScheduler.schedulePrayerAlarms(context, null);
            }
            return;
        }

        String prayerName = intent.getStringExtra("prayer_name");
        boolean isPreReminder = intent.getBooleanExtra("is_pre_reminder", false);
        
        if (prayerName == null) prayerName = "Prayer";

        String localizedPrayerName = getLocalizedPrayerName(context, prayerName);

        createNotificationChannel(context);
        
        if (isPreReminder) {
            showPreReminderNotification(context, prayerName, localizedPrayerName);
        } else {
            showNotification(context, prayerName, localizedPrayerName);

            // Auto-silent mode: activate silent and schedule restore
            SettingsManager sm = SettingsManager.getInstance(context);
            if (sm.isAutoSilentEnabled() && SilentModeManager.hasDoNotDisturbPermission(context)) {
                SilentModeManager.activateSilentMode(context);
                int duration = sm.getAutoSilentDuration();
                AlarmScheduler.scheduleRestoreRingerAlarm(context, duration);
            }
        }
    }

    private void showPreReminderNotification(Context context, String prayerName, String localizedPrayerName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "prayer_pre_reminder_channel")
                .setSmallIcon(R.mipmap.ic_launcher_round) // Replace 
                .setContentTitle(context.getString(R.string.upcoming_prayer, localizedPrayerName))
                .setContentText(localizedPrayerName + " " + context.getString(R.string.is_coming_soon))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use different ID for reminders (e.g. 2000+) to avoid overwriting Adhan notification if they overlap closely
            notificationManager.notify(prayerName.hashCode() + 1000, builder.build());
        }
    }

    private void showNotification(Context context, String prayerName, String localizedPrayerName) {
        // Intent for the Full-Screen Activity
        Intent fullScreenIntent = new Intent(context, ReminderActivity.class);
        fullScreenIntent.putExtra("prayer_name", prayerName);
        fullScreenIntent.putExtra("localized_prayer_name", localizedPrayerName);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        android.net.Uri soundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.adhan);
        String channelId = "prayer_reminder_channel_adhan_4";

        if ("Fajr".equalsIgnoreCase(prayerName) || "Subuh".equalsIgnoreCase(prayerName)) {
            soundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.adhan_fajr);
            channelId = "prayer_reminder_channel_fajr_1";
        }

        String nextName = getNextFromCurrent(prayerName);
        String localizedNextName = getLocalizedPrayerName(context, nextName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(localizedPrayerName)
                .setContentText(context.getString(R.string.next_prayer) + " " + localizedNextName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(soundUri)
                .setContentIntent(fullScreenPendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "prayer_reminder_channel_adhan_4",
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Crucial for FullScreenIntent
            );
            channel.setDescription("Channel for Prayer Time Reminders");
            
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build();
            android.net.Uri soundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.adhan);
            channel.setSound(soundUri, audioAttributes);
            
            // Fajr reminder channel - Specific Adhan sound attached
            NotificationChannel fajrChannel = new NotificationChannel(
                    "prayer_reminder_channel_fajr_1",
                    "Fajr Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            fajrChannel.setDescription("Channel for Fajr Prayer Time Reminders");
            android.net.Uri fajrSoundUri = android.net.Uri.parse(android.content.ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.adhan_fajr);
            fajrChannel.setSound(fajrSoundUri, audioAttributes);
            
            // Pre-reminder channel - Default sound
            NotificationChannel preChannel = new NotificationChannel(
                    "prayer_pre_reminder_channel",
                    "Pre-Prayer Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            preChannel.setDescription("Channel for prior notifications");
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                manager.createNotificationChannel(fajrChannel);
                manager.createNotificationChannel(preChannel);
            }
        }
    }
}
