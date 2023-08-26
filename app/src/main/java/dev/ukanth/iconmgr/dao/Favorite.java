package dev.ukanth.iconmgr.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Favorite")


public class Favorite {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "iconPkg")
    private String iconPkg;

    @ColumnInfo(name = "iconName")
    private String iconName;

    @ColumnInfo(name = "fav")
    private boolean fav;

//    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
//    private byte[] iconImage;

    @ColumnInfo(name = "Icontitle")
    private String Icontitle;

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public Favorite(String iconPkg, @NonNull String iconName, boolean fav,  String Icontitle ) {
        this.iconPkg = iconPkg;
        this.iconName = iconName;
        this.fav = fav;
        this.Icontitle = Icontitle;
    }

    public Favorite() {
    }


    @NonNull
    public String getIconPkg() {
        return iconPkg;
    }

    @NonNull
    public void setIconPkg(String iconPkg) {
        this.iconPkg = iconPkg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

//    public byte[] getIconImage() {
//        return iconImage;
//    }
//
//    public void setIconImage(byte[] iconImage) {
//        this.iconImage = iconImage;
//    }

    public String getIcontitle() {
        return Icontitle;
    }

    public void setIcontitle(String Icontitle) {
        this.Icontitle = Icontitle;
    }


}

