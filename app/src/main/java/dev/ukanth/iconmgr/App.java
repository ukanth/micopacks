package dev.ukanth.iconmgr;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.greenrobot.greendao.database.Database;

import dev.ukanth.iconmgr.dao.DaoMaster;
import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.Historydatabase;
import dev.ukanth.iconmgr.dao.IPObjdatabase;

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

//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "icons-db");
//        Database db = helper.getWritableDb();
//        daoSession = new DaoMaster(db).newSession();
//
//        DaoMaster.DevOpenHelper helper2 = new DaoMaster.DevOpenHelper(this, "history-db");
//        Database db2 = helper2.getWritableDb();
//        daoSessionHistory = new DaoMaster(db2).newSession();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        receiver = new InstallReceiver();
        registerReceiver(receiver, intentFilter);
    }

    public DaoSession getDaoSession() {
        if (daoSession != null) {
            return daoSession;
        } else {
//            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "icons-db");
//            Database db = helper.getWritableDb();
//            daoSession = new DaoMaster(db).newSession();
//            return daoSession;
            IPObjdatabase appDatabase = IPObjdatabase.getInstance(this);
            daoSession = appDatabase.IPObjdao();
            return daoSession;
        }
    }

    public DaoSession getHistoryDaoSession() {
        if (daoSessionHistory != null) {
            return daoSessionHistory;
        } else {
//            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "history-db");
//            Database db = helper.getWritableDb();
//            daoSessionHistory = new DaoMaster(db).newSession();
//            return daoSessionHistory;
            Historydatabase historydatabase = Historydatabase.getInstance(this);
            daoSessionHistory = historydatabase.Historydao();
            return daoSessionHistory;
        }
    }
}
