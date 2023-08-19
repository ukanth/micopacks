package dev.ukanth.iconmgr;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.HistoryDatabase;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.dao.IPObjDatabase;

/**
 * Created by ukanth on 13/8/17.
 */

public class App extends Application {

    private static App instance;

    private IPObjDao ipObjDao;

    private HistoryDao historyDao;

    private BroadcastReceiver receiver;


    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }


    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        receiver = new InstallReceiver();
        registerReceiver(receiver, intentFilter);

        IPObjDatabase db = IPObjDatabase.getInstance(getApplicationContext());
        ipObjDao = db.ipObjDao();

        HistoryDatabase db2 = HistoryDatabase.getInstance(getApplicationContext());
         historyDao = db2.historyDao();

    }

    public IPObjDao getIPObjDao() {
        return ipObjDao;
    }

    public HistoryDao getHistoryDao() {
        return historyDao;
    }

}