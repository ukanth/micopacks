package dev.ukanth.iconmgr;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ukanth on 24/7/17.
 */

public class Prefs {
    private static final String SORT_BY = "sort_option";
    public static final String THEME_RES_ID = "dark_theme";
    public static final String TOTAL_ICONS = "total_icons";
    public static final String NOTIFY = "notify_install";
    public static final String FAB = "enable_fab";
    public static final String PREVIEW = "preview_nonthemed";
    public static final String ROOT_TASKER = "use_root_tasker";
    public static final String SHOW_SIZE = "showpack_size";
    public static final String SHOW_AUTHOR_NAME = "show_authorName";
    public static final String SHOW_PERCENT = "showpack_percent";
    public static final String LIST_COL = "preview_col";
    public static final String IS_FIRST_TIME = "isFirstTime";
    public static final String PS = "PS";
    public static final String FAV = "show_favorites";
    public static final String ENABLE_PROMPT="use_launcher_menu";

    public static boolean isDarkTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(THEME_RES_ID, false);
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

    public static boolean showSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(SHOW_SIZE, false);
    }


    public static boolean isTotalIcons() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(TOTAL_ICONS, false);
    }

    public static boolean isNonPreview() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(PREVIEW, false);
    }

    public static boolean isNotify() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(NOTIFY, false);
    }

    public static boolean isFabShow() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(FAB, false);
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

    public static boolean showAuthorName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(SHOW_AUTHOR_NAME,false);
    }

    public static boolean showPercentage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        return prefs.getBoolean(SHOW_PERCENT, false);
    }
}
