package dev.ukanth.iconmgr;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

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
    // short background jobs that are not DB writes (details screen stats, etc.)
    private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2);


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

        AppCompatDelegate.setDefaultNightMode(Prefs.nightMode());
        // wallpaper colours only when the user opted in; the precondition is checked per activity,
        // so toggling the setting takes effect on the next recreate
        DynamicColors.applyToActivitiesIfAvailable(this, new DynamicColorsOptions.Builder()
                .setPrecondition((activity, theme) -> Prefs.useDynamicColor())
                .build());

        // Initialize shared executor for database operations
        dbExecutor = Executors.newSingleThreadExecutor();

        // single app-wide receiver for icon pack install/update/uninstall
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        receiver = new PackageBroadcast();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, intentFilter);
        }

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

    public ExecutorService getBackgroundExecutor() {
        return backgroundExecutor;
    }

}
