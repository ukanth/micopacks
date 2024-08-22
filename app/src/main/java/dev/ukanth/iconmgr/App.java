package dev.ukanth.iconmgr;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import dev.ukanth.iconmgr.dao.FavDao;
import dev.ukanth.iconmgr.dao.FavDatabase;
import dev.ukanth.iconmgr.dao.FreezeDao;
import dev.ukanth.iconmgr.dao.FreezeDatabase;
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

    private FavDao favDao;

    private FreezeDao freezeDao;
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

        FavDatabase db3 = FavDatabase.getInstance(getApplicationContext());
        favDao = db3.favDao();

        FreezeDatabase db4 = FreezeDatabase.getInstance(getApplicationContext());
        freezeDao = db4.freezeDao();
    }

    public IPObjDao getIPObjDao() {
        return ipObjDao;
    }

    public HistoryDao getHistoryDao() {
        return historyDao;
    }

    public FavDao getFavDao(){return  favDao; }

    public FreezeDao getFreezeDao(){return freezeDao; }

}