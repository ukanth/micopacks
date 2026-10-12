package dev.ukanth.iconmgr.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Saves an icon as PNG where the user can find it: Pictures/Micopacks/&lt;pack&gt; via MediaStore on
 * Android 10+ (no storage permission needed), the app's own Pictures folder on older versions.
 */
public final class IconSaver {

    private IconSaver() {
    }

    /** @return a human-readable location, or null if saving failed */
    public static String save(Context context, Bitmap bitmap, String packName, String iconName) {
        String fileName = sanitize(iconName) + ".png";
        String folder = "Micopacks/" + sanitize(packName);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + folder);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    return null;
                }
                try (OutputStream out = resolver.openOutputStream(uri)) {
                    if (out == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        resolver.delete(uri, null, null);
                        return null;
                    }
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
                return Environment.DIRECTORY_PICTURES + "/" + folder + "/" + fileName;
            } else {
                File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), folder);
                if (!dir.exists() && !dir.mkdirs()) {
                    return null;
                }
                File file = new File(dir, fileName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        return null;
                    }
                }
                return file.getAbsolutePath();
            }
        } catch (IOException | SecurityException e) {
            Log.e("IconSaver", "Saving " + fileName + " failed", e);
            return null;
        }
    }

    static String sanitize(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "icon";
        }
        return name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
