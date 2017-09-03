package dev.ukanth.iconmgr;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by ukanth on 3/9/17.
 */

public class Icon {

    private String mTitle;
    private int mRes;
    private String mPackageName;

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

    private Bitmap iconBitmap;

    public Icon(String title, int res,Bitmap bitmap) {
        mTitle = title;
        mRes = res;
        iconBitmap = bitmap;
    }

    public Icon(String title, int res, String packageName, Bitmap iconBitmap) {
        mTitle = title;
        mRes = res;
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

    public int getRes() {
        return mRes;
    }

    public String getPackageName() {
        return mPackageName;
    }

    @Override
    public boolean equals(Object object) {
        boolean res = false;
        boolean title = false;
        if (object != null && object instanceof Icon) {
            res = mRes == ((Icon) object).getRes();
            title = mTitle.equals(((Icon) object).getTitle());
        }
        return res && title;
    }
}