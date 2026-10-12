package dev.ukanth.iconmgr.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

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

    /** PackageInfo.lastUpdateTime of the pack (used for "last update" sort). */
    @ColumnInfo(name = "installTime")
    private long installTime;
    @ColumnInfo(name = "total")
    private int total;
    @ColumnInfo(name = "missed")
    private int missed;

    /**
     * Legacy JSON blob (IconAttr) from schema v1. Only read by the v1->v2 migration;
     * the values now live in real columns below.
     */
    @ColumnInfo(name = "additional")
    private String additional;

    /** User state: survives re-indexing and "Rebuild list". */
    @ColumnInfo(name = "favorite", defaultValue = "0")
    private boolean favorite;

    @ColumnInfo(name = "sizeBytes", defaultValue = "0")
    private long sizeBytes;

    /** Certificate CN of the pack's signer; null when the certificate has none. */
    @ColumnInfo(name = "author")
    private String author;

    /** Pack version the stats were computed for; a different installed version triggers re-indexing. */
    @ColumnInfo(name = "versionCode", defaultValue = "0")
    private long versionCode;

    /**
     * Drawable names (separated by '|') of this pack's icons for the home card strip: the same
     * apps across all packs, chosen by how many packs cover them. Null until computed.
     */
    @ColumnInfo(name = "previewIcons")
    private String previewIcons;

    /** InstalledApps hash that {@link #missed} was computed against. */
    @ColumnInfo(name = "appsHash", defaultValue = "0")
    private int appsHash;

    @Ignore
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

    /** Copy, so list items handed to the UI are never mutated in place (DiffUtil relies on that). */
    @Ignore
    public IPObj(IPObj other) {
        this.iconPkg = other.iconPkg;
        this.iconName = other.iconName;
        this.iconType = other.iconType;
        this.installTime = other.installTime;
        this.total = other.total;
        this.missed = other.missed;
        this.additional = other.additional;
        this.favorite = other.favorite;
        this.sizeBytes = other.sizeBytes;
        this.author = other.author;
        this.versionCode = other.versionCode;
        this.appsHash = other.appsHash;
        this.previewIcons = other.previewIcons;
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

    public void setIconPkg(@NonNull String iconPkg) {
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

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public String getPreviewIcons() {
        return previewIcons;
    }

    public void setPreviewIcons(String previewIcons) {
        this.previewIcons = previewIcons;
    }

    public int getAppsHash() {
        return appsHash;
    }

    public void setAppsHash(int appsHash) {
        this.appsHash = appsHash;
    }

    /** Field-by-field comparison for DiffUtil (equals() only compares the package). */
    public boolean sameContentAs(IPObj o) {
        return installTime == o.installTime && total == o.total && missed == o.missed
                && favorite == o.favorite && sizeBytes == o.sizeBytes && versionCode == o.versionCode
                && Objects.equals(iconName, o.iconName) && Objects.equals(author, o.author)
                && Objects.equals(previewIcons, o.previewIcons);
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
