package com.androidstarter.app.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Utility helper to handle file conversion from Uri to File, mime-type extraction,
 * and building MultipartBody.Part objects with optional progress tracking.
 */
public class FileUploadHelper {

    /**
     * Converts a Uri (from picker, etc.) into a temporary file in the cache directory.
     * This avoids Scoped Storage / content uri path resolution errors on Android 10+.
     */
    @Nullable
    public static File getFileFromUri(@NonNull Context context, @NonNull Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            if (fileName == null) {
                fileName = "temp_upload_" + System.currentTimeMillis();
            }

            File tempFile = new File(context.getCacheDir(), fileName);
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                
                if (inputStream == null) return null;

                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                return tempFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resolves the actual file name from a Content URI.
     */
    @Nullable
    public static String getFileName(@NonNull Context context, @NonNull Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    /**
     * Resolves the MIME Type for a file.
     */
    @NonNull
    public static String getMimeType(@NonNull Context context, @NonNull Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * Builds a standard non-tracked MultipartBody.Part from a File.
     */
    @NonNull
    public static MultipartBody.Part buildMultipartPart(@NonNull String paramName, @NonNull File file, @NonNull String mimeType) {
        RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
        return MultipartBody.Part.createFormData(paramName, file.getName(), requestFile);
    }

    /**
     * Builds a Progress-Tracked MultipartBody.Part from a File.
     */
    @NonNull
    public static MultipartBody.Part buildProgressMultipartPart(
            @NonNull String paramName, 
            @NonNull File file, 
            @NonNull String mimeType, 
            @NonNull ProgressRequestBody.UploadCallback callback) {
        
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(file, mimeType, callback);
        return MultipartBody.Part.createFormData(paramName, file.getName(), progressRequestBody);
    }

    /**
     * Helper to wrap a simple string value for Multipart form data fields.
     */
    @NonNull
    public static RequestBody buildStringPart(@NonNull String value) {
        return RequestBody.create(value, MediaType.parse("text/plain"));
    }
}
