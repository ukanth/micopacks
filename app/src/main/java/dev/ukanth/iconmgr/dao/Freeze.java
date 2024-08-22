package dev.ukanth.iconmgr.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Freeze {

    @PrimaryKey
    @NonNull
    private String iconPkg;

    @ColumnInfo(name = "iconName")
    private String iconName;

    public Freeze(String iconPkg, @NonNull String iconName) {
        this.iconPkg = iconPkg;
        this.iconName = iconName;
    }

    public Freeze() {
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


}
