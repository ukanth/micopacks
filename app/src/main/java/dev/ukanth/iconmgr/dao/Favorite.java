package dev.ukanth.iconmgr.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Favorite", indices = {
        @Index(value = {"iconPkg","iconName","fav","Icontitle"}, unique = true)
})


public class Favorite {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "iconPkg")
    private String iconPkg;

    @ColumnInfo(name = "iconName")
    private String iconName;

    @ColumnInfo(name = "fav")
    private boolean fav;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] iconImageData;

    public byte[] getIconImageData() {
        return iconImageData;
    }

    public void setIconImageData(byte[] iconImageData) {
        this.iconImageData = iconImageData;
    }

    @ColumnInfo(name = "Icontitle")
    private String Icontitle;

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public Favorite(String iconPkg, @NonNull String iconName, boolean fav,  String Icontitle , byte[] iconImageData) {
        this.iconPkg = iconPkg;
        this.iconName = iconName;
        this.fav = fav;
        this.Icontitle = Icontitle;
        this.iconImageData = iconImageData;
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



    public String getIcontitle() {
        return Icontitle;
    }

    public void setIcontitle(String Icontitle) {
        this.Icontitle = Icontitle;
    }


}

