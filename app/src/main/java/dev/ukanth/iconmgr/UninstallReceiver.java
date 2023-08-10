package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.HistoryDatabase;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 28/7/17.
 */

public class UninstallReceiver extends BroadcastReceiver {

    private IPObjDao ipObjDao;
    private HistoryDao historyDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (packageName != null) {
            try {
               App app = ((App) context.getApplicationContext());
                /*DaoSession daoSession = app.getDaoSession();
                DaoSession historySession = app.getHistoryDaoSession();
                HistoryDatabase db = HistoryDatabase.getInstance(context.getApplicationContext());
                HistoryDao ipObjDao = db.ipObjDao();
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
                */

                    /*List<IPObj> listPackages = MainActivity.getIconPacksList();
                    if (listPackages != null) {
                        for (IPObj pack : listPackages) {
                            if (pack != null && pack.getIconPkg() != null && pack.getIconPkg().equals(packageName)) {
                                MainActivity.getIconPacksList().remove(pack);
                                MainActivity.getAdapter().notifyDataSetChanged();
                                return;
                            }
                        }
                    }*/
               // }
                //ipObjDao.deleteByKey(packageName);
            } catch (Exception e) {
                Log.e("MICO", "Exception in UninstallReceiver" + e.getMessage(), e);
            }
        }
    }

    private History getHistory(IPObj pkgObj) {
        return new History(pkgObj.getIconPkg(), pkgObj.getIconName(), pkgObj.getIconType(),
                pkgObj.getInstallTime(), System.currentTimeMillis(), pkgObj.getTotal(), 0, pkgObj.getAdditional());
    }
}
