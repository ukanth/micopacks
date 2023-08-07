package dev.ukanth.iconmgr.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by ukanth on 13/8/17.
 */

@Entity(tableName = "IPObj", indices = {
        @Index(value = {"iconPkg", "iconType", "iconName"}, unique = true)
})

public class IPObj {

    @PrimaryKey
    @NonNull
    private String iconPkg;

    @ColumnInfo(name = "iconName")
    private String iconName;

    @ColumnInfo(name = "iconType")
    private String iconType;

    @ColumnInfo(name = "installTime")
    private long installTime;
    @ColumnInfo(name = "total")
    private int total;
    @ColumnInfo(name = "missed")
    private int missed;

    @ColumnInfo(name = "additional")
    private String additional;


    public IPObj(String iconPkg, @NonNull String iconName, String iconType, long installTime, int total, int missed, String additional) {
        this.iconPkg = iconPkg;
        this.iconName = iconName;
        this.iconType = iconType;
        this.installTime = installTime;
        this.total = total;
        this.missed = missed;
        this.additional = additional;
    }

    public IPObj() {
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    @NonNull
    public String getIconPkg() {
        return iconPkg;
    }

    @NonNull
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
}
