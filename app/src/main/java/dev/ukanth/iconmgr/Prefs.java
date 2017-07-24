package dev.ukanth.iconmgr;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ukanth on 24/7/17.
 */

public  class Prefs {
    public static final String TAG = "MICO";

    private static final String SORT_BY = "sort_option";

    private static final String THEME_RES_ID = "dark_theme";


    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(THEME_RES_ID, false);
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
