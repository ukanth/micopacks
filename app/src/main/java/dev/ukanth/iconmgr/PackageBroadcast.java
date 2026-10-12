package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import dev.ukanth.iconmgr.data.PackRepository;
import dev.ukanth.iconmgr.util.AuthorCache;
import dev.ukanth.iconmgr.util.BitmapCache;

import static dev.ukanth.iconmgr.tasker.FireReceiver.TAG;

/**
 * App-wide receiver for package install/update/uninstall; all work is done by PackRepository.
 */
public class PackageBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri inputUri = intent.getData();
        if (inputUri == null || !"package".equals(inputUri.getScheme())) {
            Log.d(TAG, "Intent scheme was not 'package'");
            return;
        }
        String packageName = inputUri.getSchemeSpecificPart();
        if (packageName == null) {
            return;
        }
        // an update sends REMOVED + ADDED with EXTRA_REPLACING; treat it as an update, not an uninstall
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        PackRepository repository = PackRepository.get();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            if (!replacing) {
                AuthorCache.getInstance().remove(packageName);
                BitmapCache.getInstance().evictPackage(packageName);
                repository.onPackageRemoved(packageName);
            }
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            if (replacing) {
                BitmapCache.getInstance().evictPackage(packageName);
            }
            repository.onPackageAdded(packageName, replacing);
        }
    }
}
