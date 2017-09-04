package dev.ukanth.iconmgr;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by ukanth on 3/9/17.
 */

public class Icon {

    private String mTitle;
    private String mPackageName;

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }


    private Bitmap iconBitmap;

    public Icon(String title) {
        mTitle = title;
    }

    public Icon(String title, Bitmap bitmap) {
        mTitle = title;
        iconBitmap = bitmap;
    }

    public Icon(String title, String packageName, Bitmap iconBitmap) {
        mTitle = title;
        mPackageName = packageName;
        this.iconBitmap = iconBitmap;
    }

    public Icon(String title, @NonNull List<Icon> icons) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Icon icon = (Icon) o;
        return mTitle.equals(icon.mTitle);
    }

    @Override
    public int hashCode() {
        return mTitle.hashCode();
    }
}