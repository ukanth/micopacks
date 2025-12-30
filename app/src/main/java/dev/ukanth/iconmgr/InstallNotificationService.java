package dev.ukanth.iconmgr;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

public class InstallNotificationService extends Service {
    private BroadcastReceiver receiver;

    public InstallNotificationService() {
    }

    @Override
    public void onCreate() {
        registerService();
    }

    private void registerService() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        receiver = new InstallReceiver();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, intentFilter);
        }
    }

    //ensure that we unregister the receiver once it's done.
    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(receiver != null) {
            unregisterReceiver(receiver);
            registerService();
        }
        return START_STICKY;
    }
}
