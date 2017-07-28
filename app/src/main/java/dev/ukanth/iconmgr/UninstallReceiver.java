package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

/**
 * Created by ukanth on 28/7/17.
 */

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        List<IconPack> listPackages = MainActivity.getIconPacksList();
        if (listPackages != null) {
            for (IconPack pack : listPackages) {
                if (pack != null && pack.packageName != null && pack.packageName.equals(packageName)) {
                    MainActivity.getIconPacksList().remove(pack);
                    MainActivity.getAdapter().notifyDataSetChanged();
                    return;
                }
            }
        }
    }
}
