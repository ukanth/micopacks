package dev.ukanth.iconmgr.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by ukanth on 13/8/17.
 */
@Entity(indexes = {
        @Index(value = "iconPkg, iconType, iconName ASC", unique = true)
})

public class IPObj implements Parcelable {

    @Id
    private String iconPkg;

    @Unique
    @NotNull
    private String iconName;
    private String iconType;
    private long installTime;
    private int total;
    private int missed;
    private String additional;

    @Generated(hash = 86537239)
    public IPObj(String iconPkg, @NotNull String iconName, String iconType, long installTime, int total, int missed, String additional) {
        this.iconPkg = iconPkg;
        this.iconName = iconName;
        this.iconType = iconType;
        this.installTime = installTime;
        this.total = total;
        this.missed = missed;
        this.additional = additional;
    }

    @Generated(hash = 66213617)
    public IPObj() {
    }

    protected IPObj(Parcel in) {
        iconPkg = in.readString();
        iconName = in.readString();
        iconType = in.readString();
        installTime = in.readLong();
        total = in.readInt();
        missed = in.readInt();
        additional = in.readString();
    }

    public static final Creator<IPObj> CREATOR = new Creator<IPObj>() {
        @Override
        public IPObj createFromParcel(Parcel in) {
            return new IPObj(in);
        }

        @Override
        public IPObj[] newArray(int size) {
            return new IPObj[size];
        }
    };

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getIconPkg() {
        return iconPkg;
    }

    public void setIconPkg(String iconPkg) {
        this.iconPkg = iconPkg;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(long installTime) {
        this.installTime = installTime;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getMissed() {
        return missed;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPObj obj = (IPObj) o;

        return iconPkg.equals(obj.iconPkg);

    }

    @Override
    public int hashCode() {
        return iconPkg.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(iconPkg);
        dest.writeString(iconName);
        dest.writeString(iconType);
        dest.writeLong(installTime);
        dest.writeInt(total);
        dest.writeInt(missed);
        dest.writeString(additional);
    }
}
