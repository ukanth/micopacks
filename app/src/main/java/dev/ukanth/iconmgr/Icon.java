package dev.ukanth.iconmgr;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by ukanth on 3/9/17.
 */

public class Icon implements Parcelable {

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

    private Icon(Parcel in){
        this.mTitle = in.readString();
        this.mPackageName = in.readString();
        this.iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mPackageName);
        dest.writeValue(iconBitmap);
    }

    public static final Parcelable.Creator<Icon> CREATOR = new Parcelable.Creator<Icon>() {

        @Override
        public Icon createFromParcel(Parcel source) {
            return new Icon(source);
        }

        @Override
        public Icon[] newArray(int size) {
            return new Icon[size];
        }
    };

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