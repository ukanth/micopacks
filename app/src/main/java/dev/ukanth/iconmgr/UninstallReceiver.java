package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 28/7/17.
 */

public class UninstallReceiver extends BroadcastReceiver {

    private IPObjDao ipObjDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        try {
            App app = ((App) context.getApplicationContext());
            DaoSession daoSession = app.getDaoSession();
            ipObjDao = daoSession.getIPObjDao();
            IPObj pkgObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(packageName)).unique();
            if (pkgObj != null) {
                IconAttr attr = new Gson().fromJson(pkgObj.getAdditional(), IconAttr.class);
                attr.setDeleted(true);
                pkgObj.setAdditional(attr.toString());
                ipObjDao.update(pkgObj);
                List<IPObj> listPackages = MainActivity.getIconPacksList();
                if (listPackages != null) {
                    for (IPObj pack : listPackages) {
                        if (pack != null && pack.getIconPkg() != null && pack.getIconPkg().equals(packageName)) {
                            MainActivity.getIconPacksList().remove(pack);
                            MainActivity.getAdapter().notifyDataSetChanged();
                            return;
                        }
                    }
                }
            }
            //ipObjDao.deleteByKey(packageName);

        } catch (Exception e) {
            Log.e("MICO", "Exception in UninstallReceiver" + e.getMessage());
        }
    }
}
