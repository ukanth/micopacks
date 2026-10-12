package dev.ukanth.iconmgr.home;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import dev.ukanth.iconmgr.R;

/**
 * Something the user can do to a pack from the Packs list: by swiping a row (each direction is
 * configurable in Settings) or from the long-press actions sheet. The key is what Settings stores.
 */
public enum PackAction {
    OFF("off", R.string.swipe_off, 0, 0),
    APPLY("apply", R.string.action_apply, R.drawable.ic_apply, androidx.appcompat.R.attr.colorPrimary),
    FAVORITE("favorite", R.string.icon_favorite, R.drawable.ic_star_black_24dp, com.google.android.material.R.attr.colorSecondary),
    OPEN("open", R.string.open_pack_app, R.drawable.ic_launch, com.google.android.material.R.attr.colorTertiary),
    PLAY_STORE("playstore", R.string.action_playstore, R.drawable.ic_playstore, com.google.android.material.R.attr.colorTertiary),
    UNINSTALL("uninstall", R.string.action_uninstall, R.drawable.ic_delete_red, androidx.appcompat.R.attr.colorError);

    public final String key;
    @StringRes
    public final int label;
    @DrawableRes
    public final int icon;
    /** Theme colour behind a row while it is swiped with this action. */
    @AttrRes
    public final int color;

    PackAction(String key, int label, int icon, int color) {
        this.key = key;
        this.label = label;
        this.icon = icon;
        this.color = color;
    }

    public static PackAction fromKey(String key) {
        for (PackAction action : values()) {
            if (action.key.equals(key)) {
                return action;
            }
        }
        return OFF;
    }
}
