package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ukanth on 24/7/17.
 */

public class Prefs {
    public static final String TAG = "MICO";

    private static final String SORT_BY = "sort_option";
    public static final String THEME_RES_ID = "dark_theme";
    public static final String TOTAL_ICONS = "total_icons";

    public static final String INCLUDE_SYS = "include_system";
    public static final String NOTIFY = "notify_install";

    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(THEME_RES_ID, false);
    }

    /*public static boolean isCalcPercent(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(CAL_PERCENT, false);
    }*/

    public static boolean isIncludeSystem(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(INCLUDE_SYS, false);
    }

    public static boolean isTotalIcons(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(TOTAL_ICONS, false);
    }

    public static boolean isNotify(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(NOTIFY, false);
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
