package dev.ukanth.iconmgr;

import android.app.Application;

import org.greenrobot.greendao.database.Database;

import dev.ukanth.iconmgr.dao.DaoMaster;
import dev.ukanth.iconmgr.dao.DaoSession;

/**
 * Created by ukanth on 13/8/17.
 */

public class App extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "icons-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
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
}
