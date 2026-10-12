package dev.ukanth.iconmgr;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

/**
 * Created by ukanth on 24/7/17.
 */

public class Prefs {
    private static final String SORT_BY = "sort_option";
    /** Legacy boolean (v5.0 and earlier); migrated to {@link #THEME_MODE}. */
    public static final String THEME_RES_ID = "dark_theme";
    public static final String THEME_MODE = "theme_mode";
    public static final String DYNAMIC_COLOR = "dynamic_color";
    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String NOTIFY = "notify_install";
    public static final String ROOT_TASKER = "use_root_tasker";
    public static final String LIST_COL = "preview_col";
    public static final String IS_FIRST_TIME = "isFirstTime";
    public static final String PS = "PS";
    public static final String FAV = "show_favorites";
    public static final String ENABLE_PROMPT="use_launcher_menu";
    public static final String AUTO_THEMING = "enable_auto_theming";
    public static final String SWIPE_RIGHT = "swipe_right";
    public static final String SWIPE_LEFT = "swipe_left";
    private static final String SWIPE_HINT_SHOWN = "swipe_hint_shown";

    /**
     * "system", "light" or "dark". Users who set the old dark-mode switch keep their explicit
     * choice; everyone else follows the system.
     */
    public static String themeMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String mode = prefs.getString(THEME_MODE, null);
        if (mode == null) {
            mode = prefs.contains(THEME_RES_ID)
                    ? (prefs.getBoolean(THEME_RES_ID, false) ? THEME_DARK : THEME_LIGHT)
                    : THEME_SYSTEM;
            prefs.edit().putString(THEME_MODE, mode).apply();
        }
        return mode;
    }

    public static int nightMode() {
        switch (themeMode()) {
            case THEME_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case THEME_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    /** Material You wallpaper colours (Android 12+); brand colours otherwise. */
    public static boolean useDynamicColor() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(DYNAMIC_COLOR, false);
    }

    public static boolean useRoot() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(ROOT_TASKER, true);
    }

    public static boolean usePrompt() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(ENABLE_PROMPT, false);
    }

    public static boolean useFavorite() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(FAV, false);
    }





    public static boolean isNotify() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(NOTIFY, false);
    }



    public static boolean isFirstTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(IS_FIRST_TIME, true);
    }

    public static String sortBy() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getString(SORT_BY, "s0");
    }

    public static void sortBy(String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        prefs.edit().putString(SORT_BY, value).commit();
    }

    public static int getCol() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        String currentValue = prefs.getString(LIST_COL, "5");
        return Integer.parseInt(currentValue);
    }

    public static void setFirstRun(boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        prefs.edit().putBoolean(IS_FIRST_TIME, b).commit();
    }

    public static void setLicensed(boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        prefs.edit().putBoolean(PS, b).commit();
    }

    public static boolean isPS() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(PS, false);
    }


    
    public static boolean isAutoThemingEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(AUTO_THEMING, false);
    }

    /** {@link dev.ukanth.iconmgr.home.PackAction} key for a right swipe on a pack row. */
    public static String swipeRight() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getString(SWIPE_RIGHT, "apply");
    }

    /** {@link dev.ukanth.iconmgr.home.PackAction} key for a left swipe on a pack row. */
    public static String swipeLeft() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getString(SWIPE_LEFT, "favorite");
    }

    public static boolean swipeHintShown() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(SWIPE_HINT_SHOWN, false);
    }

    public static void setSwipeHintShown() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        prefs.edit().putBoolean(SWIPE_HINT_SHOWN, true).apply();
    }
}
