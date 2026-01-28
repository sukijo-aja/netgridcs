package com.mosleemapp.app.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mosleemapp.app.data.local.PrayerTimeEntity;
import com.mosleemapp.app.receivers.PrayerAlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler {

    public static void schedulePrayerAlarms(Context context, PrayerTimeEntity entity) {
        if (entity == null) return;
        
        SettingsManager sm = SettingsManager.getInstance(context);
        if (!sm.isReminderEnabled()) return;

        int preReminder = sm.getPrePrayerReminderMinutes();

        schedulePrayer(context, sm, "Fajr", entity.fajr, 1, preReminder);
        schedulePrayer(context, sm, "Dhuhr", entity.dhuhr, 2, preReminder);
        schedulePrayer(context, sm, "Asr", entity.asr, 3, preReminder);
        schedulePrayer(context, sm, "Maghrib", entity.maghrib, 4, preReminder);
        schedulePrayer(context, sm, "Isha", entity.isha, 5, preReminder);
    }

    private static void schedulePrayer(Context context, SettingsManager sm, String name, String time, int code, int preReminder) {
        if (sm.isPrayerAlarmEnabled(name)) {
            int correction = sm.getPrayerTimeCorrection(name);
            
            // Main Adhan Alarm
            scheduleAlarm(context, name, time, code, correction, false);
            
            // Pre-Reminder Alarm
            if (preReminder > 0) {
                scheduleAlarm(context, name, time, code + 10, correction - preReminder, true);
            }
        }
    }
    
    public static void cancelAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Cancel Adhan (1-5) and Pre-Reminders (11-15)
        for (int i = 1; i <= 5; i++) {
            cancelPendingIntent(context, alarmManager, i);
            cancelPendingIntent(context, alarmManager, i + 10);
        }
    }

    private static void cancelPendingIntent(Context context, AlarmManager am, int requestCode) {
        Intent intent = new Intent(context, PrayerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pendingIntent);
    }

    @SuppressLint("ScheduleExactAlarm")
    private static void scheduleAlarm(Context context, String prayerName, String timeStr, int requestCode, int minutesAdjustment, boolean isPreReminder) {
        // timeStr format usually "HH:mm (WIB)" or "HH:mm"
        // Clean it
        String cleanTime = timeStr.split(" ")[0];

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = sdf.parse(cleanTime);
            if (date == null) return;

            Calendar now = Calendar.getInstance();
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(date);
            
            // Set fields to matches today/target date
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, alarmTime.get(Calendar.HOUR_OF_DAY));
            target.set(Calendar.MINUTE, alarmTime.get(Calendar.MINUTE));
            target.set(Calendar.SECOND, 0);
            
            // Apply adjustment (correction +/- preReminder)
            target.add(Calendar.MINUTE, minutesAdjustment);

            // If time is passed for today, ignore (or schedule for tomorrow if we had logic for it)
            if (target.before(now)) {
                return; 
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, PrayerAlarmReceiver.class);
            intent.putExtra("prayer_name", prayerName);
            intent.putExtra("is_pre_reminder", isPreReminder);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                // Use setExactAndAllowWhileIdle for reliable timing
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
                Log.d("AlarmScheduler", "Scheduled " + prayerName + (isPreReminder ? " (Pre)" : "") + " at " + target.getTime());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
