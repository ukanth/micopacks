package dev.ukanth.iconmgr;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.Util;

import static android.content.Context.NOTIFICATION_SERVICE;
import static dev.ukanth.iconmgr.util.Util.getCurrentLauncher;

/**
 * Created by ukanth on 16/8/17.
 */

public class ActionReceiver extends BroadcastReceiver {
    public static final String APPLY_ACTION = "APPLY_ACTION";
    public static final String PREVIEW_ACTION = "PREVIEW_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        String pkgName = bundle.getString("pkg");

        if (APPLY_ACTION.equals(action)) {
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.cancel(Util.hash(pkgName));
            String currentLauncher = getCurrentLauncher(context);
            if (currentLauncher != null) {
                LauncherHelper.apply(context, pkgName, currentLauncher);
            } else {
                Toast.makeText(context, context.getString(R.string.nodefault), Toast.LENGTH_LONG).show();
            }
        } else if (PREVIEW_ACTION.equals(action)) {
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.cancel(Util.hash(pkgName));
            Intent previewIntent = new Intent(context, IconPreviewActivity.class);
            previewIntent.putExtra("pkg", pkgName);
            previewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(previewIntent);
        }
    }
}
