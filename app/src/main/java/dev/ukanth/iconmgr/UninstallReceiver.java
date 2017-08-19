package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

/**
 * Created by ukanth on 28/7/17.
 */

public class UninstallReceiver extends BroadcastReceiver {

    private IPObjDao ipObjDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        try {
            ipObjDao = Util.getDAO(context);
            IPObj ipObj = new IPObj();
            ipObj.setIconPkg(packageName);
            if(ipObjDao.hasKey(ipObj)) {
                ipObjDao.deleteByKey(packageName);
                List<IPObj> listPackages = MainActivity.getIconPacksList();
                if (listPackages != null) {
                    for (IPObj pack : listPackages) {
                        if (pack != null && pack.getIconPkg() != null && pack.getIconPkg().equals(packageName)) {
                            Log.i("MICO", "Found icon pack and uninstalling");
                            MainActivity.getIconPacksList().remove(pack);
                            MainActivity.getAdapter().notifyDataSetChanged();
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MICO", "Exception in UninstallReceiver" + e.getMessage());
        }
    }
}
