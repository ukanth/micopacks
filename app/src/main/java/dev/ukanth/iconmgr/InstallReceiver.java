package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObjDao;

/**
 * Created by ukanth on 28/7/17.
 */

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        try {
            App app = ((App) context.getApplicationContext());
            DaoSession daoSession = app.getDaoSession();
            IPObjDao ipObjDao = daoSession.getIPObjDao();
            IconPackManager iconPackManager = new IconPackManager(context);
            iconPackManager.insertIconPack(ipObjDao, packageName);
        } catch (Exception e) {
            Log.e("MICO", "Exception in InstallReceiver" + e.getMessage());
        }
    }
}
