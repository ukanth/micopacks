package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;

/**
 * Created by ukanth on 3/9/17.
 */

public class DrawableHelper {

    public static int getResourceId(@NonNull Context context, String resName) {
        try {
            return context.getResources().getIdentifier(
                    resName, "drawable", context.getPackageName());
        } catch (Exception ignored) {}
        return -1;
    }


    public static Drawable getAppIcon(@NonNull Context context, ResolveInfo info) {
        try {
            return info.activityInfo.loadIcon(context.getPackageManager());
        } catch (OutOfMemoryError | Exception e) {
            return null;
        }
    }

    @Nullable
    public static Drawable getHighQualityIcon(@NonNull Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);

            Resources resources = packageManager.getResourcesForApplication(packageName);
            int density = DisplayMetrics.DENSITY_XXHIGH;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                density = DisplayMetrics.DENSITY_XXXHIGH;
            }

            Drawable drawable = ResourcesCompat.getDrawableForDensity(
                    resources, info.icon, density, null);
            if (drawable != null) return drawable;
            return info.loadIcon(packageManager);
        } catch (Exception | OutOfMemoryError e) {
        }
        return null;
    }
}
