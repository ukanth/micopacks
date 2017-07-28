package dev.ukanth.iconmgr;

/**
 * Created by ukanth on 17/7/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class IconPackManager {
    private Context mContext;

    public IconPackManager(Context c) {
        mContext = c;
    }

    public HashMap<String, IconPack> getAvailableIconPacks() {
        HashMap<String, IconPack> iconPacks = new HashMap<String, IconPack>();
        PackageManager pm = mContext.getPackageManager();
        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES |
                PackageManager.GET_UNINSTALLED_PACKAGES;

        List<ApplicationInfo> packages = pm.getInstalledApplications(flags);
        ArrayList<String> packageList = new ArrayList<>();
        for(ApplicationInfo info: packages) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                // System application
                if(Prefs.isIncludeSystem(mContext)) {
                    packageList.add(info.packageName);
                }
            } else {
                // Installed by user
                packageList.add(info.packageName);
            }

        }
        loadIconPack("GO", iconPacks, pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA), pm,packageList);
        loadIconPack("GO", iconPacks, pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), PackageManager.GET_META_DATA), pm,packageList);
        return iconPacks;
    }

    private void loadIconPack(String key, HashMap<String, IconPack> iconPacks, List<ResolveInfo> rinfo, PackageManager pm,ArrayList packageList) {
        for (ResolveInfo ri : rinfo) {
            IconPack ip = new IconPack(ri.activityInfo.packageName, mContext,packageList);
            ip.packageName = ri.activityInfo.packageName;
            ip.type = key;
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(ip.packageName, PackageManager.GET_META_DATA);
                ip.installTime = pm.getPackageInfo(ip.packageName,0).firstInstallTime;
                ip.name = mContext.getPackageManager().getApplicationLabel(ai).toString();
                iconPacks.put(ip.packageName, ip);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

    }
}
