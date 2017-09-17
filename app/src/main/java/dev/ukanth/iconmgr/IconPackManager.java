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
    private List<IPObj> returnList;
    private HashSet<String> unique;


    public IconPackManager(Context c) {
        mContext = c;
    }

    public List<IPObj> updateIconPacks(IPObjDao ipObjDao, boolean delete) {
        returnList = new ArrayList<>();
        PackageManager pm = mContext.getPackageManager();
        int flags = PackageManager.GET_META_DATA |
                PackageManager.GET_SHARED_LIBRARY_FILES;
        unique = new HashSet();


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

        loadIconPack("GO", rinfo, pm, ipObjDao);
        return returnList;
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


    private void loadIconPack(String key, List<ResolveInfo> rinfo, PackageManager pm, IPObjDao ipObjDao) {
        IPObj obj;
        IconPackUtil ip = new IconPackUtil();
        ApplicationInfo ai = null;
        for (ResolveInfo ri : rinfo) {
            String pkgName = ri.activityInfo.packageName;
            if (!unique.contains(pkgName)) {
                unique.add(pkgName);
                IconAttr attr = new IconAttr();
                obj = new IPObj();
                obj.setIconPkg(pkgName);
                obj.setIconType(key);
                if (!ipObjDao.hasKey(obj)) {
                    try {
                        ai = pm.getApplicationInfo(obj.getIconPkg(), PackageManager.GET_META_DATA);
                        obj.setInstallTime(pm.getPackageInfo(obj.getIconPkg(), 0).lastUpdateTime);
                        obj.setIconName(mContext.getPackageManager().getApplicationLabel(ai).toString());
                        obj.setTotal(ip.calcTotal(mContext, obj.getIconPkg()));
                        attr.setDeleted(false);
                        obj.setMissed(ip.getMissingApps(mContext, obj.getIconPkg(), Util.getInstalledApps(mContext)).size());
                        attr.setSize(Util.getApkSize(mContext, obj.getIconPkg()));
                        obj.setAdditional(attr.toString());
                        ipObjDao.insert(obj);
                    } catch (PackageManager.NameNotFoundException | android.database.sqlite.SQLiteConstraintException sqe) {
                        sqe.printStackTrace();
                    }
                } else {
                    try {
                        obj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
                        if (obj.getMissed() == 0 || obj.getAdditional() == null) {
                            attr.setDeleted(false);
                            obj.setMissed(ip.getMissingApps(mContext, obj.getIconPkg(), Util.getInstalledApps(mContext)).size());
                            attr.setSize(Util.getApkSize(mContext, obj.getIconPkg()));
                            obj.setAdditional(attr.toString());
                            ipObjDao.update(obj);
                        }
                        returnList.add(obj);
                        Log.i("MICO", "Skipping " + obj.getIconPkg());
                    } catch (PackageManager.NameNotFoundException | android.database.sqlite.SQLiteConstraintException sqe) {
                        sqe.printStackTrace();
                    }
                }
            }
        }
    }
}
