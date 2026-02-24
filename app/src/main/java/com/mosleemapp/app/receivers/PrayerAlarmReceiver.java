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

    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle restore-ringer alarm
        if (ACTION_RESTORE_RINGER.equals(intent.getAction())) {
            SilentModeManager.restoreRingerMode(context);
            return;
        }

        String prayerName = intent.getStringExtra("prayer_name");
        boolean isPreReminder = intent.getBooleanExtra("is_pre_reminder", false);
        
        if (prayerName == null) prayerName = "Prayer";

        createNotificationChannel(context);
        
        if (isPreReminder) {
            showPreReminderNotification(context, prayerName);
        } else {
            showNotification(context, prayerName);

            // Auto-silent mode: activate silent and schedule restore
            SettingsManager sm = SettingsManager.getInstance(context);
            if (sm.isAutoSilentEnabled() && SilentModeManager.hasDoNotDisturbPermission(context)) {
                SilentModeManager.activateSilentMode(context);
                int duration = sm.getAutoSilentDuration();
                AlarmScheduler.scheduleRestoreRingerAlarm(context, duration);
            }
        }
    }

    private void showPreReminderNotification(Context context, String prayerName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Replace 
                .setContentTitle("Upcoming Prayer")
                .setContentText(prayerName + " is coming soon")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use different ID for reminders (e.g. 2000+) to avoid overwriting Adhan notification if they overlap closely
            notificationManager.notify(prayerName.hashCode() + 1000, builder.build());
        }
    }

    private void showNotification(Context context, String prayerName) {
        // Intent for the Full-Screen Activity
        Intent fullScreenIntent = new Intent(context, ReminderActivity.class);
        fullScreenIntent.putExtra("prayer_name", prayerName);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Replace with valid icon
                .setContentTitle("Prayer Time")
                .setContentText("It's time for " + prayerName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // Crucial for FullScreenIntent
            );
            channel.setDescription("Channel for Prayer Time Reminders");
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
