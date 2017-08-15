package dev.ukanth.iconmgr.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by ukanth on 13/8/17.
 */
@Entity(indexes = {
        @Index(value = "iconPkg, iconType, iconName ASC", unique = true)
})

public class IPObj {

    @Id
    private String iconPkg;

    @NotNull
    private String iconName;
    private String iconType;
    private long installTime;
    private int total;

    private int missed;

    @Generated(hash = 1623016430)
    public IPObj(String iconPkg, @NotNull String iconName, String iconType, long installTime, int total,
            int missed) {
        this.iconPkg = iconPkg;
        this.iconName = iconName;
        this.iconType = iconType;
        this.installTime = installTime;
        this.total = total;
        this.missed = missed;
    }

    @Generated(hash = 66213617)
    public IPObj() {
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

}
