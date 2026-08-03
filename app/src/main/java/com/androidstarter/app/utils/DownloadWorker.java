package com.androidstarter.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker extends Worker {

    public static final String KEY_URL = "key_url";
    public static final String KEY_FILE_NAME = "key_file_name";
    public static final String KEY_FILE_PATH = "key_file_path";
    public static final String KEY_PROGRESS = "key_progress";
    
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 101;
    
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder;

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "File Downloads",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
        
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Downloading file")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }

    @NonNull
    @Override
    public Result doWork() {
        String urlString = getInputData().getString(KEY_URL);
        String fileName = getInputData().getString(KEY_FILE_NAME);

        if (urlString == null || fileName == null) {
            return Result.failure();
        }

        // Set worker as foreground to prevent Android system from killing it
        setForegroundAsync(createForegroundInfo(0));

        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Result.failure();
            }

            int fileLength = connection.getContentLength();
            input = connection.getInputStream();

            File outputDir = getApplicationContext().getExternalFilesDir(null);
            if (outputDir == null) {
                outputDir = getApplicationContext().getFilesDir();
            }
            File outputFile = new File(outputDir, fileName);
            output = new FileOutputStream(outputFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                if (isStopped()) {
                    return Result.failure();
                }
                total += count;
                if (fileLength > 0) {
                    int progress = (int) (total * 100 / fileLength);
                    setProgressAsync(new Data.Builder().putInt(KEY_PROGRESS, progress).build());
                    updateNotification(progress);
                }
                output.write(data, 0, count);
            }

            // Success
            Data outputData = new Data.Builder()
                    .putString(KEY_FILE_PATH, outputFile.getAbsolutePath())
                    .build();
            
            showSuccessNotification(fileName);
            return Result.success(outputData);

        } catch (Exception e) {
            e.printStackTrace();
            showFailureNotification(fileName);
            return Result.failure();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(int progress) {
        notificationBuilder.setProgress(100, progress, false)
                           .setContentText(progress + "% completed");
        return new ForegroundInfo(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotification(int progress) {
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder
                .setProgress(100, progress, false)
                .setContentText(progress + "% completed")
                .build());
    }

    private void showSuccessNotification(String fileName) {
        NotificationCompat.Builder successBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Download Complete")
                .setContentText(fileName)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setOngoing(false);
        notificationManager.notify((int) System.currentTimeMillis(), successBuilder.build());
    }

    private void showFailureNotification(String fileName) {
        NotificationCompat.Builder failureBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Download Failed")
                .setContentText("Failed to download " + fileName)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setAutoCancel(true)
                .setOngoing(false);
        notificationManager.notify((int) System.currentTimeMillis(), failureBuilder.build());
    }
}
