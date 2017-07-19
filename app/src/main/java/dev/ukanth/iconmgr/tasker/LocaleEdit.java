package dev.ukanth.iconmgr.tasker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

public class LocaleEdit extends AppCompatActivity {
    //public static final String LOCALE_BRIGHTNESS = "dev.ukanth.ufirewall.plugin.LocaleEdit.ACTIVE_PROFLE";

    private boolean mIsCancelled = false;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        BundleScrubber.scrub(getIntent());
        BundleScrubber.scrub(getIntent().getBundleExtra(
                dev.ukanth.iconmgr.tasker.Intent.EXTRA_BUNDLE));

        //setContentView(R.layout.tasker_profile);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.tasker_toolbar);

        //setSupportActionBar(toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }

    private void setupTitleApi11() {
        CharSequence callingApplicationLabel = null;
        try {
            callingApplicationLabel = getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(getCallingPackage(),
                            0));
        } catch (final NameNotFoundException e) {
        }
        if (null != callingApplicationLabel) {
            setTitle(callingApplicationLabel);
        }
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
           /* case R.id.twofortyfouram_locale_menu_dontsave:
                mIsCancelled = true;
                finish();
                return true;
            case R.id.twofortyfouram_locale_menu_save:
                finish();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }


    /*@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.twofortyfouram_locale_help_save_dontsave, menu);
        return true;
    }*/

    @Override
    public void finish() {
        if (mIsCancelled) {
            setResult(RESULT_CANCELED);
        } else {
            /*RadioGroup group = (RadioGroup) findViewById(R.id.radioProfiles);
            int selectedId = group.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) findViewById(selectedId);
            String action = radioButton.getText().toString();
            final Intent resultIntent = new Intent();
            if (!G.isProfileMigrated()) {
                int idx = group.indexOfChild(radioButton);
                resultIntent.putExtra(dev.ukanth.iconmgr.tasker.Intent.EXTRA_BUNDLE, PluginBundleManager.generateBundle(getApplicationContext(), idx + "::" + action));
            } else {
                resultIntent.putExtra(dev.ukanth.iconmgr.tasker.Intent.EXTRA_BUNDLE, PluginBundleManager.generateBundle(getApplicationContext(), selectedId + "::" + action));
            }
            resultIntent.putExtra(dev.ukanth.iconmgr.tasker.Intent.EXTRA_STRING_BLURB, action);
            setResult(RESULT_OK, resultIntent);*/
        }
        super.finish();
    }
}
