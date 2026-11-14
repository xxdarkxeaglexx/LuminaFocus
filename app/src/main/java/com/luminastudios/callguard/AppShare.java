package com.luminastudios.callguard;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

final class AppShare {
    private static final String MIME = "application/vnd.android.package-archive";

    static void start(Activity activity, Button button) {
        button.setEnabled(false);
        button.setText("Preparing APK…");
        new Thread(() -> {
            try {
                Uri apk = prepare(activity);
                activity.runOnUiThread(() -> {
                    if (activity.isDestroyed() || activity.isFinishing()) return;
                    restore(button);
                    Intent send = new Intent(Intent.ACTION_SEND).setType(MIME)
                        .putExtra(Intent.EXTRA_STREAM, apk)
                        
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    send.setClipData(ClipData.newUri(activity.getContentResolver(), "Lumina Focus One", apk));
                    try { activity.startActivity(Intent.createChooser(send, "Share Lumina Focus One")); }
                    catch (android.content.ActivityNotFoundException e) {
                        Toast.makeText(activity, "APK saved in Downloads/Lumina Focus. Share it from Files.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    if (activity.isDestroyed() || activity.isFinishing()) return;
                    restore(button);
                    Toast.makeText(activity, "Could not prepare the APK. Check free storage and try again.", Toast.LENGTH_LONG).show();
                });
            }
        }, "Lumina APK share").start();
    }

    private static void restore(Button button) {
        button.setEnabled(true);
        button.setText("Share app");
    }

    private static byte[] digest(InputStream input) throws Exception {
        if (input == null) throw new IOException("Missing stream");
        try (InputStream stream = input) {
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536];
            int n;
            while ((n = stream.read(buffer)) != -1) hash.update(buffer, 0, n);
            return hash.digest();
        }
    }

    private static synchronized Uri prepare(Activity app) throws Exception {
        File installed = new File(app.getApplicationInfo().sourceDir);
        String version = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
        SharedPreferences cache = app.getSharedPreferences("installer-share", 0);
        ContentResolver resolver = app.getContentResolver();
        String saved = cache.getString(version, null);
        if (saved != null) {
            Uri existing = Uri.parse(saved);
            try {
                if (MessageDigest.isEqual(digest(new FileInputStream(installed)),
                        digest(resolver.openInputStream(existing)))) return existing;
            } catch (IOException | SecurityException ignored) { }
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "LuminaFocus_One_" + version + ".apk");
        values.put(MediaStore.Downloads.MIME_TYPE, MIME);
        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/Lumina Focus/");
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        Uri created = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (created == null) throw new IOException("Could not create download");
        try {
            try (InputStream in = new FileInputStream(installed); OutputStream out = resolver.openOutputStream(created)) {
                if (out == null) throw new IOException("Could not open download");
                byte[] buffer = new byte[65536];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            }
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            if (resolver.update(created, values, null, null) != 1) throw new IOException("Could not publish download");
            cache.edit().putString(version, created.toString()).apply();
            return created;
        } catch (Exception e) {
            resolver.delete(created, null, null);
            throw e;
        }
    }
}
