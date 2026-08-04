package com.androidstarter.app.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Custom RequestBody wrapper that tracks write progress for uploads.
 * Automatically dispatches progress updates to the main thread.
 */
public class ProgressRequestBody extends RequestBody {

    private final File file;
    private final String contentType;
    private final UploadCallback callback;
    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallback {
        void onProgress(int progress);
        void onError(Throwable t);
    }

    public ProgressRequestBody(File file, String contentType, UploadCallback callback) {
        this.file = file;
        this.contentType = contentType;
        this.callback = callback;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long uploaded = 0;

        Handler mainHandler = new Handler(Looper.getMainLooper());

        try (FileInputStream in = new FileInputStream(file)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                sink.write(buffer, 0, read);
                uploaded += read;

                final int progress = (int) (100 * uploaded / fileLength);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onProgress(progress);
                    }
                });
            }
        } catch (final IOException e) {
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onError(e);
                }
            });
            throw e;
        }
    }
}
