package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.dao.IPObjDatabase;
import dev.ukanth.iconmgr.util.AppCache;
import dev.ukanth.iconmgr.util.AuthorCache;
import dev.ukanth.iconmgr.util.BitmapCache;

import static dev.ukanth.iconmgr.tasker.FireReceiver.TAG;

public class PackageBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Uri inputUri = Uri.parse(intent.getDataString());

        if (!inputUri.getScheme().equals("package")) {
            Log.d(TAG, "Intent scheme was not 'package'");
            return;
        }
        String packageName = intent.getData().getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            // Invalidate caches when package is removed
            AppCache.getInstance().invalidate();
            AuthorCache.getInstance().remove(packageName);
            BitmapCache.getInstance().evictPackage(packageName);
            
            if (packageName != null) {
                // Run database operations on background thread
                App.getInstance().getDbExecutor().execute(() -> {
                    try {
                        IPObjDao ipObjDao = App.getInstance().getIPObjDao();
                        HistoryDao historyDao = App.getInstance().getHistoryDao();

                        IPObj pkgObj = ipObjDao.getByIconPkg(packageName);

                        if (pkgObj != null) {
                            ipObjDao.delete(pkgObj);
                            Intent intentNotify = new Intent();
                            intentNotify.setAction("updatelist");
                            intentNotify.putExtra("pkgName", packageName);
                            context.sendBroadcast(intentNotify);
                            historyDao.insertOrReplace(getHistory(pkgObj));
                        }
                    } catch (Exception e) {
                        Log.e("MICO", "Exception in UninstallReceiver" + e.getMessage(), e);
                    }
                });
            }

        } else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) || Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())) {
            // Invalidate caches when package is added or changed
            AppCache.getInstance().invalidate();
            
            if (packageName != null) {
                // Run database operations on background thread
                App.getInstance().getDbExecutor().execute(() -> {
                    try {
                        IPObjDatabase db = IPObjDatabase.getInstance(context.getApplicationContext());
                        IPObjDao ipObjDao = db.ipObjDao();
                        new IconPackManager().insertIconPack(ipObjDao, packageName);
                    } catch (Exception e) {
                        Log.e("MICO", "Exception in InstallReceiver" + e.getMessage());
                    }
                });
            }
        }
    }

    private History getHistory(IPObj pkgObj) {
        return new History(pkgObj.getIconPkg(), pkgObj.getIconName(), pkgObj.getIconType(),
                pkgObj.getInstallTime(), System.currentTimeMillis(), pkgObj.getTotal(), 0, pkgObj.getAdditional());
    }
}
