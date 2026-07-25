package com.mosleemapp.app.utils.app;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.mosleemapp.app.workers.ReminderWatchdogWorker;

import java.util.concurrent.TimeUnit;

public class ReminderWatchdog {

    public static void start(Context context) {

        PeriodicWorkRequest work =
                new PeriodicWorkRequest.Builder(
                        ReminderWatchdogWorker.class,
                        15,
                        TimeUnit.MINUTES
                ).build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        "reminder_watchdog",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        work
                );
    }
}