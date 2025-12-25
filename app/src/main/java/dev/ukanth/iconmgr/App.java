package dev.ukanth.iconmgr;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.dao.FavDao;
import dev.ukanth.iconmgr.dao.FavDatabase;
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


    private BroadcastReceiver receiver;
    private ExecutorService dbExecutor;


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

        // Initialize shared executor for database operations
        dbExecutor = Executors.newSingleThreadExecutor();

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


    }

    public IPObjDao getIPObjDao() {
        return ipObjDao;
    }

    public HistoryDao getHistoryDao() {
        return historyDao;
    }

    public FavDao getFavDao(){return  favDao; }

    public ExecutorService getDbExecutor() {
        return dbExecutor;
    }

}