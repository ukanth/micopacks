package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

import static dev.ukanth.iconmgr.tasker.FireReceiver.TAG;

public class PackageBroadcast extends BroadcastReceiver {

    private IPObjDao ipObjDao;
    private HistoryDao historyDao;


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
                    App app = ((App) context.getApplicationContext());
                    DaoSession daoSession = app.getDaoSession();
                    DaoSession historySession = app.getHistoryDaoSession();
                    ipObjDao = daoSession.getIPObjDao();
                    historyDao = historySession.getHistoryDao();
                    IPObj pkgObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(packageName)).unique();
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
                    App app = ((App) context.getApplicationContext());
                    DaoSession daoSession = app.getDaoSession();
                    IPObjDao ipObjDao = daoSession.getIPObjDao();
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
