package com.androidstarter.app.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Reusable Image Picker utility supporting Camera and Gallery.
 * 
 * Usage in Activity:
 *   ImagePickerHelper picker = new ImagePickerHelper(this, uri -> {
 *       // handle selected image URI
 *       imageView.setImageURI(uri);
 *   });
 *   picker.openCamera();   // or
 *   picker.openGallery();  // or
 *   picker.showChooser();
 */
public class ImagePickerHelper {

    public interface OnImagePickedListener {
        void onImagePicked(Uri imageUri);
    }

    private final Context context;
    private final OnImagePickedListener listener;
    private final ActivityResultLauncher<Intent> galleryLauncher;
    private final ActivityResultLauncher<Uri> cameraLauncher;
    private final ActivityResultLauncher<String> permissionLauncher;
    private Uri cameraImageUri;
    private boolean pendingCamera = false;

    /**
     * Initialize from an Activity.
     */
    public ImagePickerHelper(AppCompatActivity activity, OnImagePickedListener listener) {
        this.context = activity;
        this.listener = listener;

        galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK
                            && result.getData() != null && result.getData().getData() != null) {
                        listener.onImagePicked(result.getData().getData());
                    }
                });

        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        listener.onImagePicked(cameraImageUri);
                    }
                });

        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted && pendingCamera) {
                        pendingCamera = false;
                        launchCamera();
                    }
                });
    }

    /**
     * Initialize from a Fragment.
     */
    public ImagePickerHelper(Fragment fragment, OnImagePickedListener listener) {
        this.context = fragment.requireContext();
        this.listener = listener;

        galleryLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK
                            && result.getData() != null && result.getData().getData() != null) {
                        listener.onImagePicked(result.getData().getData());
                    }
                });

        cameraLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        listener.onImagePicked(cameraImageUri);
                    }
                });

        permissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted && pendingCamera) {
                        pendingCamera = false;
                        launchCamera();
                    }
                });
    }

    /** Open device gallery to pick an image. */
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    /** Open camera to take a photo. Handles CAMERA permission automatically. */
    public void openCamera() {
        if (!PermissionHelper.hasPermission(context, Manifest.permission.CAMERA)) {
            pendingCamera = true;
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            launchCamera();
        }
    }

    /** Show a chooser dialog between Camera and Gallery. */
    public void showChooser() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                .setTitle("Select Image")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    photoFile
            );
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }
}
