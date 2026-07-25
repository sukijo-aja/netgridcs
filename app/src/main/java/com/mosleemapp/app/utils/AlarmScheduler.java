package com.mosleemapp.app.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mosleemapp.app.data.local.PrayerTimeEntity;
import com.mosleemapp.app.receivers.PrayerAlarmReceiver;
import com.mosleemapp.app.utils.app.SettingsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler {

    public static void schedulePrayerAlarms(Context context, PrayerTimeEntity entity) {
        if (entity == null) {
            com.mosleemapp.app.data.local.AppDatabase.databaseWriteExecutor.execute(() -> {
                java.util.List<PrayerTimeEntity> allEntities = com.mosleemapp.app.data.local.AppDatabase.getDatabase(context).prayerDao().getAllPrayerTimes();
                if (allEntities != null && !allEntities.isEmpty()) {
                    for (PrayerTimeEntity dbEntity : allEntities) {
                        processAlarms(context, dbEntity);
                    }
                }
            });
            return;
        }
        
        processAlarms(context, entity);
    }
    
    private static void processAlarms(Context context, PrayerTimeEntity entity) {
        SettingsManager sm = SettingsManager.getInstance(context);
        if (!sm.isReminderEnabled() || entity == null || entity.date == null) return;

        int preReminder = sm.getPrePrayerReminderMinutes();
        String dateStr = entity.date;
        
        int dayOfYear = 0;
        try {
            Date d = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
            if (d != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                dayOfYear = c.get(Calendar.DAY_OF_YEAR);
            }
        } catch (ParseException e) {}
        
        int baseCode = dayOfYear * 100;

        schedulePrayer(context, sm, "Fajr", entity.fajr, dateStr, baseCode + 1, preReminder);
        schedulePrayer(context, sm, "Dhuhr", entity.dhuhr, dateStr, baseCode + 2, preReminder);
        schedulePrayer(context, sm, "Asr", entity.asr, dateStr, baseCode + 3, preReminder);
        schedulePrayer(context, sm, "Maghrib", entity.maghrib, dateStr, baseCode + 4, preReminder);
        schedulePrayer(context, sm, "Isha", entity.isha, dateStr, baseCode + 5, preReminder);
    }

    private static void schedulePrayer(Context context, SettingsManager sm, String name, String time, String dateStr, int code, int preReminder) {
        if (sm.isPrayerAlarmEnabled(name)) {
            int correction = sm.getPrayerTimeCorrection(name);
            
            // Main Adhan Alarm
            scheduleAlarm(context, name, time, dateStr, code, correction, false);
            
            // Pre-Reminder Alarm
            if (preReminder > 0) {
                scheduleAlarm(context, name, time, dateStr, code + 10, correction - preReminder, true);
            }
        }
    }
    
    public static void cancelAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        com.mosleemapp.app.data.local.AppDatabase.databaseWriteExecutor.execute(() -> {
            java.util.List<PrayerTimeEntity> allEntities = com.mosleemapp.app.data.local.AppDatabase.getDatabase(context).prayerDao().getAllPrayerTimes();
            if (allEntities != null) {
                for (PrayerTimeEntity entity : allEntities) {
                    if (entity.date != null) {
                        try {
                            Date d = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(entity.date);
                            if (d != null) {
                                Calendar c = Calendar.getInstance();
                                c.setTime(d);
                                int baseCode = c.get(Calendar.DAY_OF_YEAR) * 100;
                                for (int i = 1; i <= 5; i++) {
                                    cancelPendingIntent(context, alarmManager, baseCode + i);
                                    cancelPendingIntent(context, alarmManager, baseCode + i + 10);
                                }
                            }
                        } catch (ParseException e) {}
                    }
                }
            }
        });

        // Cancel Adhan (1-5) and Pre-Reminders (11-15) fallback for old alarms
        for (int i = 1; i <= 5; i++) {
            cancelPendingIntent(context, alarmManager, i);
            cancelPendingIntent(context, alarmManager, i + 10);
        }
        // Cancel restore-ringer alarm
        cancelRestoreRingerAlarm(context, alarmManager);
    }

    /**
     * Schedule an alarm to restore the ringer mode after the given duration.
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleRestoreRingerAlarm(Context context, int durationMinutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, PrayerAlarmReceiver.class);
        intent.setAction(PrayerAlarmReceiver.ACTION_RESTORE_RINGER);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                99, // unique request code for restore-ringer
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar target = Calendar.getInstance();
        target.add(Calendar.MINUTE, durationMinutes);

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e("AlarmScheduler", "SecurityException scheduling restore ringer alarm", e);
        }
    }

    private static void cancelRestoreRingerAlarm(Context context, AlarmManager am) {
        Intent intent = new Intent(context, PrayerAlarmReceiver.class);
        intent.setAction(PrayerAlarmReceiver.ACTION_RESTORE_RINGER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                99,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pendingIntent);
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
    private static void scheduleAlarm(Context context, String prayerName, String timeStr, String dateStr, int requestCode, int minutesAdjustment, boolean isPreReminder) {
        // timeStr format usually "HH:mm (WIB)" or "HH:mm"
        // Clean it
        String cleanTime = timeStr.split(" ")[0];

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = sdf.parse(cleanTime);
            if (date == null) return;

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(date);
            
            Calendar target = Calendar.getInstance();
            
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    SimpleDateFormat dateSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date entityDate = dateSdf.parse(dateStr);
                    if (entityDate != null) {
                        Calendar dateCal = Calendar.getInstance();
                        dateCal.setTime(entityDate);
                        target.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
                        target.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
                        target.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Set fields to matches today/target date
            target.set(Calendar.HOUR_OF_DAY, alarmTime.get(Calendar.HOUR_OF_DAY));
            target.set(Calendar.MINUTE, alarmTime.get(Calendar.MINUTE));
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);
            
            // Apply adjustment (correction +/- preReminder)
            target.add(Calendar.MINUTE, minutesAdjustment);

            Calendar now = Calendar.getInstance();

            // If time is passed for the scheduled target, ignore
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
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
                    } else {
                        // Use setExactAndAllowWhileIdle for reliable timing
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
                    }
                } catch (SecurityException e) {
                    Log.e("AlarmScheduler", "SecurityException scheduling alarm", e);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
