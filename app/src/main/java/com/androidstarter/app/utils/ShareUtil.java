package com.androidstarter.app.utils;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareUtil {

    public static void shareText(Context context, String title, String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    public static void shareViewAsImage(Context context, View view, String title) {
        Bitmap bitmap = getBitmapFromView(view);
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/dua_image.png"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            File imagePath = new File(context.getCacheDir(), "images");
            File newFile = new File(imagePath, "dua_image.png");
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                context.startActivity(Intent.createChooser(shareIntent, "Share Image via"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.getBackground().draw(canvas); // Draw background
        view.draw(canvas);
        return returnedBitmap;
    }
}
