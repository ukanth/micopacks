package dev.ukanth.iconmgr;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.color.DynamicColors;

import dev.ukanth.iconmgr.util.WindowInsetsHelper;

import static dev.ukanth.iconmgr.Prefs.DYNAMIC_COLOR;
import static dev.ukanth.iconmgr.Prefs.NOTIFY;
import static dev.ukanth.iconmgr.Prefs.THEME_MODE;

/**
 * App settings (AndroidX Preference). Keys are unchanged from the old PreferenceActivity, so
 * existing settings carry over.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        WindowInsetsHelper.applyToContent(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new GeneralPreferenceFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // make sure the migrated theme value is stored before the list shows it
            Prefs.themeMode();
            setPreferencesFromResource(R.xml.pref_general, rootKey);
            Preference dynamic = findPreference(DYNAMIC_COLOR);
            if (dynamic != null) {
                dynamic.setVisible(DynamicColors.isDynamicColorAvailable());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key == null) {
                return;
            }
            if (key.equals(THEME_MODE)) {
                // recreates every open activity in the new mode
                AppCompatDelegate.setDefaultNightMode(Prefs.nightMode());
            }
            if (key.equals(DYNAMIC_COLOR)) {
                // DynamicColors is applied when an activity is created
                MainActivity.setReloadTheme(true);
                requireActivity().recreate();
            }
            if (key.equals(NOTIFY) && sharedPreferences.getBoolean(NOTIFY, false)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && requireActivity().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requireActivity().requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            // Packs list settings (swipes, favourites first) are re-read when the list resumes
        }
    }
}
