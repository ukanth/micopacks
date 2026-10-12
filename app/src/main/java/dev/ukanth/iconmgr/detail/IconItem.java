package dev.ukanth.iconmgr.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * One cell of the pack detail grid: either an icon from the pack, or an installed app the pack
 * does not cover. Holds names only; bitmaps are loaded when the cell is on screen.
 */
public final class IconItem {

    public enum Kind {
        /** A drawable from the icon pack */
        PACK_ICON,
        /** An installed app with no icon in the pack */
        MISSING_APP
    }

    @NonNull
    public final Kind kind;
    @NonNull
    public final String title;
    /** Pack drawable name (PACK_ICON only) */
    @Nullable
    public final String drawable;
    /** Installed app package, when the item stands for an app */
    @Nullable
    public final String appPackage;
    /** Icon pack the drawable comes from, when a grid mixes packs (icon search); else null */
    @Nullable
    public final String packPackage;

    private IconItem(@NonNull Kind kind, @NonNull String title, @Nullable String drawable, @Nullable String appPackage,
                     @Nullable String packPackage) {
        this.kind = kind;
        this.title = title;
        this.drawable = drawable;
        this.appPackage = appPackage;
        this.packPackage = packPackage;
    }

    public static IconItem packIcon(String title, String drawable, @Nullable String appPackage) {
        return new IconItem(Kind.PACK_ICON, title, drawable, appPackage, null);
    }

    /** A drawable from a specific pack, for grids showing several packs. */
    public static IconItem fromPack(String packPackage, String drawable) {
        return new IconItem(Kind.PACK_ICON, drawable, drawable, null, packPackage);
    }

    public static IconItem missingApp(String label, String appPackage) {
        return new IconItem(Kind.MISSING_APP, label, null, appPackage, null);
    }

    /** Stable identity for DiffUtil and the bitmap cache. */
    public String key() {
        return kind == Kind.PACK_ICON ? "d:" + packPackage + ":" + drawable + ":" + appPackage : "a:" + appPackage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IconItem)) return false;
        IconItem other = (IconItem) o;
        return kind == other.kind && title.equals(other.title)
                && Objects.equals(drawable, other.drawable) && Objects.equals(appPackage, other.appPackage)
                && Objects.equals(packPackage, other.packPackage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, title, drawable, appPackage, packPackage);
    }
}
