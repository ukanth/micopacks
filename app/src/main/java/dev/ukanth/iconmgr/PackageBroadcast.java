package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.dao.IPObjDatabase;

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
            if (packageName != null) {
                try {
                    IPObjDatabase db = IPObjDatabase.getInstance(context.getApplicationContext());
                    IPObjDao ipObjDao = db.ipObjDao();

                    DaoSession historySession = app.getHistoryDaoSession();
                    historyDao = historySession.getHistoryDao();
                    IPObj pkgObj = ipObjDao.getByIconPkg(packageName);
                    if (pkgObj != null) {
                        //delete from install db to history
                        ipObjDao.deleteByKey(packageName);
                        Intent intentNotify = new Intent();
                        intentNotify.setAction("updatelist");
                        intentNotify.putExtra("pkgName", packageName);
                        context.sendBroadcast(intentNotify);
                        historyDao.insertOrReplace(getHistory(pkgObj));
                    }
                    //ipObjDao.deleteByKey(packageName);
                } catch (Exception e) {
                    Log.e("MICO", "Exception in UninstallReceiver" + e.getMessage(), e);
                }
            }

        } else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) || Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())) {
            if (packageName != null) {
                try {
                    IPObjDatabase db = IPObjDatabase.getInstance(context.getApplicationContext());
                    IPObjDao ipObjDao = db.ipObjDao();
                    new IconPackManager().insertIconPack(ipObjDao, packageName);
                } catch (Exception e) {
                    Log.e("MICO", "Exception in InstallReceiver" + e.getMessage());
                }
            }
        }
    }

    private History getHistory(IPObj pkgObj) {
        return new History(pkgObj.getIconPkg(), pkgObj.getIconName(), pkgObj.getIconType(),
                pkgObj.getInstallTime(), System.currentTimeMillis(), pkgObj.getTotal(), 0, pkgObj.getAdditional());
    }
}
