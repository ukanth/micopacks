package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 28/7/17.
 */

public class UninstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (packageName != null) {
            try {


                IPObjDao ipObjDao = App.getInstance().getIPObjDao();
               HistoryDao historyDao = App.getInstance().getHistoryDao();

                IPObj pkgObj = ipObjDao.getByIconPkg(packageName);
                if(pkgObj!=null){
                    ipObjDao.delete(pkgObj);
                    Intent intentNotify = new Intent();
                    intentNotify.setAction("updatelist");
                    intentNotify.putExtra("pkgName", packageName);
                    context.sendBroadcast(intentNotify);
                    historyDao.insertOrReplace(getHistory(pkgObj));
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
                }
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
