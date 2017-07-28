package dev.ukanth.iconmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ukanth on 28/7/17.
 */

public class UninstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        for(IconPack pack: MainActivity.getIconPacksList()){
            if(pack.packageName.equals(packageName)) {
                MainActivity.getIconPacksList().remove(pack);
                MainActivity.getAdapter().notifyDataSetChanged();
                return;
            }
        }
    }
}
