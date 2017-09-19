package dev.ukanth.iconmgr;

import android.content.Context;
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
    public static final String SHOW_PERCENT = "showpack_percent";
    public static final String LIST_COL = "preview_col";
    public static final String IS_FIRST_TIME = "isFirstTime";
    public static final String PS = "PS";

    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(THEME_RES_ID, false);
    }

    public static boolean useRoot(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(ROOT_TASKER, true);
    }

    public static boolean showSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SHOW_SIZE, false);
    }


    public static boolean isTotalIcons(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(TOTAL_ICONS, false);
    }

    public static boolean isNonPreview(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREVIEW, false);
    }

    public static boolean isNotify(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(NOTIFY, false);
    }

    public static boolean isFabShow(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(FAB, false);
    }


    public static boolean isFirstTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(IS_FIRST_TIME, true);
    }

    public static String sortBy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(SORT_BY, "s0");
    }

    public static void sortBy(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(SORT_BY, value).commit();
    }

    public static int getCol(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String currentValue = prefs.getString(LIST_COL, "5");
        return Integer.parseInt(currentValue);
    }

    public static void setFirstRun(Context context, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(IS_FIRST_TIME, b).commit();
    }

    public static void setLicensed(Context context, boolean b) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PS, b).commit();
    }

    public static boolean isPS(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PS, false);
    }

    public static boolean showPercentage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SHOW_PERCENT, false);
    }
}
