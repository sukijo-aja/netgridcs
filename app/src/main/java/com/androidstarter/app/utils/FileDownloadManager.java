package com.androidstarter.app.utils;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.UUID;

/**
 * Robust WorkManager-based File Downloader utility.
 * Automatically runs in the background, handles network reconnects,
 * displays a system progress notification, and reports LiveData status back to UI.
 *
 * Usage:
 *   FileDownloadManager manager = FileDownloadManager.getInstance(context);
 *   UUID downloadId = manager.downloadFile("https://example.com/file.zip", "my_file.zip", true);
 *   
 *   manager.getDownloadStatus(downloadId).observe(this, workInfo -> {
 *       if (workInfo != null) {
 *           int progress = workInfo.getProgress().getInt(DownloadWorker.KEY_PROGRESS, 0);
 *           if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
 *               String path = workInfo.getOutputData().getString(DownloadWorker.KEY_FILE_PATH);
 *           }
 *       }
 *   });
 */
public class FileDownloadManager {

    private static FileDownloadManager instance;
    private final WorkManager workManager;

    private FileDownloadManager(Context context) {
        workManager = WorkManager.getInstance(context.getApplicationContext());
    }

    public static synchronized FileDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new FileDownloadManager(context);
        }
        return instance;
    }

    /**
     * Enqueue a new file download task.
     *
     * @param url            The remote URL to download.
     * @param fileName       The name to save the file under in app-specific directory.
     * @param requireWifiOnly If true, download only starts when connected to unmetered WiFi.
     * @return Unique UUID representing the WorkRequest to query progress.
     */
    public UUID downloadFile(String url, String fileName, boolean requireWifiOnly) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(requireWifiOnly ? NetworkType.UNMETERED : NetworkType.CONNECTED)
                .build();

        Data inputData = new Data.Builder()
                .putString(DownloadWorker.KEY_URL, url)
                .putString(DownloadWorker.KEY_FILE_NAME, fileName)
                .build();

        OneTimeWorkRequest downloadRequest = new OneTimeWorkRequest.Builder(DownloadWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("DownloadTask")
                .build();

        workManager.enqueue(downloadRequest);
        return downloadRequest.getId();
    }

    /**
     * Get a LiveData object to monitor download progress and completion.
     */
    public LiveData<WorkInfo> getDownloadStatus(UUID taskId) {
        return workManager.getWorkInfoByIdLiveData(taskId);
    }

    /**
     * Cancel an ongoing download task.
     */
    public void cancelDownload(UUID taskId) {
        workManager.cancelWorkById(taskId);
    }

    /**
     * Cancel all enqueued/active download tasks.
     */
    public void cancelAllDownloads() {
        workManager.cancelAllWorkByTag("DownloadTask");
    }
}
