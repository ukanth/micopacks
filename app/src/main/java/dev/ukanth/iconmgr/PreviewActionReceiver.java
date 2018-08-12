package dev.ukanth.iconmgr;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import dev.ukanth.iconmgr.util.LauncherHelper;

import static android.content.Context.NOTIFICATION_SERVICE;
import static dev.ukanth.iconmgr.util.Util.getCurrentLauncher;

/**
 * Created by ukanth on 16/8/17.
 */

public class PreviewActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        String pkgName = bundle.getString("pkg");

       if ("dev.ukanth.iconmgr.PREVIEW_ACTION".equals(action)) {
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            cancelNotification(context, notificationmanager,pkgName);
            Intent previewIntent = new Intent(context, IconPreviewActivity.class);
            previewIntent.putExtra("pkg", pkgName);
            previewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(previewIntent);
        }
    }

    private void cancelNotification(Context context,NotificationManager notificationmanager, String pkgName) {
        int uid = 0;
        try {
            uid = context.getPackageManager().getApplicationInfo(pkgName, 0).uid;
            notificationmanager.cancel(uid);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }
}
