package dev.ukanth.iconmgr;

/**
 * Created by ukanth on 17/7/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

public class IconPackManager {
    private Context mContext;
    private HashSet<String> unique;

    public IconPackManager(Context c) {
        mContext = c;
    }

    public List<IPObj> updateIconPacks(IPObjDao ipObjDao) {
        PackageManager pm = mContext.getPackageManager();
        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES;
        unique = new HashSet();
        List<IPObj> installedIconPacks = new ArrayList<>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(flags);
        ArrayList<String> packageList = new ArrayList<>();
        for (ApplicationInfo info : packages) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                // System application
                packageList.add(info.packageName);
            } else {
                // Installed by user
                packageList.add(info.packageName);
            }
        }
        List<ResolveInfo> rinfo = pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA);
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA));

        loadIconPack("GO", rinfo, pm, ipObjDao, installedIconPacks);

        return installedIconPacks;
    }


    public void insertIconPack(IPObjDao ipObjDao, String packageName) {
        PackageManager pm = mContext.getPackageManager();
        IconPackUtil ip = new IconPackUtil();
        //detect if it's iconpack or not
        List<ResolveInfo> rinfo = pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA);
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA));
        for (ResolveInfo ri : rinfo) {
            if (ri.activityInfo.packageName.equals(packageName)) {
                IPObj obj = new IPObj();
                IconAttr attr = new IconAttr();
                obj.setIconPkg(packageName);
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    obj.setIconType("GO");
                    obj.setInstallTime(pm.getPackageInfo(obj.getIconPkg(), 0).lastUpdateTime);
                    obj.setIconName(mContext.getPackageManager().getApplicationLabel(ai).toString());
                    obj.setTotal(ip.calcTotal(mContext, obj.getIconPkg()));
                    attr.setDeleted(false);
                    attr.setSize(Util.getApkSize(mContext, packageName));
                    obj.setAdditional(attr.toString());
                    ipObjDao.insert(obj);
                    Util.showNotification(mContext, packageName);
                    IconDetails.process(mContext, packageName, AsyncTask.THREAD_POOL_EXECUTOR, null, "MISSED");
                } catch (Exception e) {
                    Log.e("MICO", "Exception in InstallReceiver" + e.getMessage());
                }
                break;
            }
        }
    }


    private void loadIconPack(String key, List<ResolveInfo> rinfo, PackageManager pm, IPObjDao ipObjDao, List<IPObj> installedIconPacks) {
        IconPackUtil ip = new IconPackUtil();
        ApplicationInfo ai = null;
        for (ResolveInfo ri : rinfo) {
            String pkgName = ri.activityInfo.packageName;
            if (!unique.contains(pkgName)) {
                unique.add(pkgName);
                IconAttr attr = new IconAttr();
                IPObj obj2 = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
                if (obj2 == null) {
                    try {
                        IPObj obj = new IPObj();
                        obj.setIconPkg(pkgName);
                        ai = pm.getApplicationInfo(obj.getIconPkg(), PackageManager.GET_META_DATA);
                        obj.setIconType(key);
                        obj.setInstallTime(pm.getPackageInfo(obj.getIconPkg(), 0).lastUpdateTime);
                        obj.setIconName(mContext.getPackageManager().getApplicationLabel(ai).toString());
                        obj.setTotal(ip.calcTotal(mContext, obj.getIconPkg()));
                        attr.setDeleted(false);
                        obj.setMissed(ip.getMissingApps(mContext, obj.getIconPkg(), Util.getInstalledApps(mContext)).size());
                        attr.setSize(Util.getApkSize(mContext, obj.getIconPkg()));
                        obj.setAdditional(attr.toString());
                        ipObjDao.insert(obj);
                        installedIconPacks.add(obj);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    try {
                        if (obj2.getMissed() == 0 || obj2.getAdditional() == null) {
                            attr.setDeleted(false);
                            obj2.setMissed(ip.getMissingApps(mContext, obj2.getIconPkg(), Util.getInstalledApps(mContext)).size());
                            attr.setSize(Util.getApkSize(mContext, obj2.getIconPkg()));
                            obj2.setAdditional(attr.toString());
                            ipObjDao.update(obj2);
                        }
                        installedIconPacks.add(obj2);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }
}
