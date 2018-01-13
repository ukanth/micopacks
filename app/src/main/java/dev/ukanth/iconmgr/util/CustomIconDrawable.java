package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

/**
 * Created by ukanth on 13/1/18.
 */

public class CustomIconDrawable extends Drawable {

    private final Context mContext;
    private final Resources mResources;
    private final Drawable mOriginalIcon;
    private Drawable mIconBack = null;
    private Drawable mIconUpon = null;
    private Bitmap mIconMask = null;
    private float mScale = 1f;
    private String packageName;

    public CustomIconDrawable(Context context, String packageName, Drawable mOriginalIcon)
            throws PackageManager.NameNotFoundException {
        mContext = context;
        mResources = context.getPackageManager().getResourcesForApplication(packageName);
        this.packageName = packageName;
        this.mOriginalIcon = mOriginalIcon;
    }

    private Drawable getDrawable(String name) {
        try {
            return mResources.getDrawable(getIconRes(name));
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    private int getIconRes(String name) {
        return mResources.getIdentifier(name, "drawable", packageName);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

    }

    private Drawable getMaskedIcon(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);

        float scaledWidth = width * mScale, scaledHeight = height * mScale;
        float horizontalPadding = (width - scaledWidth) / 2;
        float verticalPadding = (height - scaledHeight) / 2;

        mOriginalIcon.setBounds((int) horizontalPadding, (int) verticalPadding,
                (int) (scaledWidth + horizontalPadding), (int) (scaledHeight + horizontalPadding));
        mOriginalIcon.draw(canvas);

        if (mIconMask != null) {
            Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            Bitmap scaledMask = Bitmap.createScaledBitmap(mIconMask, width, height, false);
            canvas.drawBitmap(scaledMask, 0, 0, clearPaint);
        }

        return new BitmapDrawable(mContext.getResources(), bitmap);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
