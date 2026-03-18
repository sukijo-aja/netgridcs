package com.mosleemapp.app.workers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mosleemapp.app.receivers.PrayerAlarmReceiver;

public class ReminderWatchdogWorker extends Worker {

    public ReminderWatchdogWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {

        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        checkAlarm();

        return Result.success();
    }

    private void checkAlarm() {

        Context context = getApplicationContext();

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, PrayerAlarmReceiver.class);

        PendingIntent pi =
                PendingIntent.getBroadcast(
                        context,
                        1001,
                        intent,
                        PendingIntent.FLAG_NO_CREATE |
                        PendingIntent.FLAG_IMMUTABLE
                );

        if (pi == null) {

            long next = System.currentTimeMillis() + 60000;

//            ReminderScheduler.schedule(context, next);
        }
    }
}