package dev.ukanth.iconmgr;

/**
 * Created by ukanth on 17/7/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.HashMap;
import java.util.List;

public class IconPackManager {
    private Context mContext;

    public IconPackManager(Context c) {
        mContext = c;
    }

    public HashMap<String, IconPack> getAvailableIconPacks(boolean forceReload) {
        HashMap<String, IconPack> iconPacks = new HashMap<String, IconPack>();
        PackageManager pm = mContext.getPackageManager();
        loadIconPack("GO", iconPacks, pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA), pm);
        loadIconPack("GO", iconPacks, pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), PackageManager.GET_META_DATA), pm);
        return iconPacks;
    }

    private void loadIconPack(String key, HashMap<String, IconPack> iconPacks, List<ResolveInfo> rinfo, PackageManager pm) {
        for (ResolveInfo ri : rinfo) {
            IconPack ip = new IconPack(ri.activityInfo.packageName, mContext);
            ip.packageName = ri.activityInfo.packageName;
            ip.type = key;
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(ip.packageName, PackageManager.GET_META_DATA);
                ip.name = mContext.getPackageManager().getApplicationLabel(ai).toString();
                iconPacks.put(ip.packageName, ip);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

    }
}
