package dev.ukanth.iconmgr;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.greenrobot.greendao.database.Database;

import dev.ukanth.iconmgr.dao.DaoMaster;
import dev.ukanth.iconmgr.dao.DaoSession;

/**
 * Created by ukanth on 13/8/17.
 */

public class App extends Application {

    private static App instance;

    private BroadcastReceiver receiver;


    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    private DaoSession daoSession;
    private DaoSession daoSessionHistory;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "icons-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        DaoMaster.DevOpenHelper helper2 = new DaoMaster.DevOpenHelper(this, "history-db");
        Database db2 = helper2.getWritableDb();
        daoSessionHistory = new DaoMaster(db2).newSession();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        receiver = new InstallReceiver();
        registerReceiver(receiver, intentFilter);
    }

    public DaoSession getDaoSession() {
        if (daoSession != null) {
            return daoSession;
        } else {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "icons-db");
            Database db = helper.getWritableDb();
            daoSession = new DaoMaster(db).newSession();
            return daoSession;
        }
    }

    public DaoSession getHistoryDaoSession() {
        if (daoSessionHistory != null) {
            return daoSessionHistory;
        } else {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "history-db");
            Database db = helper.getWritableDb();
            daoSessionHistory = new DaoMaster(db).newSession();
            return daoSessionHistory;
        }
    }
}
