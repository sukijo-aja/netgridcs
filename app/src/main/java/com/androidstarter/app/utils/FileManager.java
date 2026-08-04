package com.androidstarter.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 * Robust local File Management utility.
 * Handles storage size checking, formatting, file copy/delete operations,
 * cache cleanup, and opening files securely via FileProvider.
 */
public class FileManager {

    /**
     * Format byte size to readable string (e.g. 1.2 MB, 450 KB).
     */
    @NonNull
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get extension of a file.
     */
    @NonNull
    public static String getFileExtension(@NonNull File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1).toLowerCase();
    }

    /**
     * Copy a file from source location to destination.
     */
    public static boolean copyFile(@NonNull File source, @NonNull File destination) {
        try (FileChannel inChannel = new FileInputStream(source).getChannel();
             FileChannel outChannel = new FileOutputStream(destination).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Safely delete a file or directory recursively.
     */
    public static boolean deleteFile(@Nullable File fileOrDirectory) {
        if (fileOrDirectory != null && fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                File[] files = fileOrDirectory.listFiles();
                if (files != null) {
                    for (File child : files) {
                        deleteFile(child);
                    }
                }
            }
            return fileOrDirectory.delete();
        }
        return false;
    }

    /**
     * Clean cache directory of the application.
     */
    public static void clearCache(@NonNull Context context) {
        try {
            deleteFile(context.getCacheDir());
            File externalCache = context.getExternalCacheDir();
            if (externalCache != null) {
                deleteFile(externalCache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check available internal storage space in bytes.
     */
    public static long getAvailableInternalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * Checks if the device has enough space for a file of a specific size.
     *
     * @param requiredBytes Bytes needed.
     * @return True if there's enough space.
     */
    public static boolean hasEnoughStorage(long requiredBytes) {
        return getAvailableInternalStorage() > requiredBytes;
    }

    /**
     * Securely opens a file via System Intent using FileProvider.
     * Handles MIME type resolution automatically.
     *
     * @param context Context.
     * @param file    The file to open.
     * @return True if the intent was started successfully, false otherwise.
     */
    public static boolean openFile(@NonNull Context context, @NonNull File file) {
        if (!file.exists()) return false;

        try {
            String authority = context.getPackageName() + ".fileprovider";
            Uri uri = FileProvider.getUriForFile(context, authority, file);

            String extension = getFileExtension(file);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType == null) {
                mimeType = "*/*";
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
