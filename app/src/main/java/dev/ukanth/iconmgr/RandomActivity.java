package dev.ukanth.iconmgr;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

/**
 * Invisible activity that applies a pack (random when no "pack" extra is given).
 * Used by launcher shortcuts and the install notification's Apply action.
 */
public class RandomActivity extends AppCompatActivity {

    private boolean applied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String pkgName = getIntent().getStringExtra("pack");
        if (getIntent().hasExtra(Util.EXTRA_NOTIFICATION_ID)) {
            NotificationManagerCompat.from(this).cancel(getIntent().getIntExtra(Util.EXTRA_NOTIFICATION_ID, 0));
        }

        // Run database operations on background thread
        App.getInstance().getDbExecutor().execute(() -> {
            IPObjDao ipObjDao = App.getInstance().getIPObjDao();
            IPObj ipObj;
            if (pkgName == null || pkgName.isEmpty()) {
                ipObj = Util.getRandomInstalledIconPack(ipObjDao);
            } else {
                ipObj = ipObjDao.getByIconPkg(pkgName);
            }
            final IPObj finalIpObj = ipObj;
            runOnUiThread(() -> {
                if (isFinishing()) {
                    return;
                }
                Util.determineApply(RandomActivity.this, finalIpObj);
                applied = true;
                // If apply only showed a toast, nothing takes focus away; close right away.
                // If it opened a dialog (launcher picker, manual-apply help), wait for it to close.
                getWindow().getDecorView().postDelayed(() -> {
                    if (hasWindowFocus()) {
                        finish();
                    }
                }, 300);
            });
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // focus returns here once a dialog shown by apply is dismissed
        if (hasFocus && applied) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // the launcher (or Play Store) came to the front
        if (applied) {
            finish();
        }
    }
}
