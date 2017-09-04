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

    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(THEME_RES_ID, false);
    }

    public static boolean isTotalIcons(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(TOTAL_ICONS, false);
    }

    public static boolean isNotify(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(NOTIFY, false);
    }

    public static boolean isFabShow(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(FAB, false);
    }

    public static String sortBy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(SORT_BY, "s0");
    }

    public static void sortBy(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(SORT_BY, value).commit();
    }
}
