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

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.util.Util;

public class IconPackManager {
    private Context mContext;
    private HashSet<String> unique;
    private List<String> excludePackages;

    public IconPackManager() {
        mContext = App.getContext();
    }

    public List<IPObj> updateIconPacks(IPObjDao ipObjDao, boolean forceReload, MaterialDialog dialog) {
        if (forceReload) {
            ipObjDao.deleteAll();
        }

        excludePackages = Util.getExcludedPackages();

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

        loadIconPack(rinfo, ipObjDao, installedIconPacks);
        return installedIconPacks;
    }


    public void insertIconPack(final IPObjDao ipObjDao, final String packageName) {
        final PackageManager pm = mContext.getPackageManager();
        final IconPackUtil ip = new IconPackUtil();
        //detect if it's iconpack or not
        List<ResolveInfo> rinfo = pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA);
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.novalauncher.THEME"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.teslacoilsw.launcher.THEME"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.anddoes.launcher.THEME"), PackageManager.GET_META_DATA));
        rinfo.addAll(pm.queryIntentActivities(new Intent("com.fede.launcher.THEME_ICONPACK"), PackageManager.GET_META_DATA));
        List<String> excludedPackage = Util.getExcludedPackages();
        for (ResolveInfo ri : rinfo) {
            if (ri.activityInfo.packageName.equals(packageName) && !excludePackages.contains(packageName)) {
                IPObj obj = new IPObj();
                IconAttr attr = new IconAttr();
                obj.setIconPkg(packageName);
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    obj.setIconType("GO");
                    obj.setInstallTime(pm.getPackageInfo(obj.getIconPkg(), 0).lastUpdateTime);
                    String name = mContext.getPackageManager().getApplicationLabel(ai).toString();
                    obj.setIconName(name);
                    obj.setTotal(ip.calcTotal(obj.getIconPkg()));
                    attr.setDeleted(false);
                    attr.setSize(Util.getApkSize(packageName));
                    obj.setAdditional(attr.toString());
                    ipObjDao.insert(obj);
                    Util.showNotification(packageName, name);
                    IconDetails.process(packageName, AsyncTask.THREAD_POOL_EXECUTOR, null, "MISSED");
                    break;
                } catch (Exception e) {
                    Log.e("MICO", "Exception in InstallReceiver" + e.getMessage(), e);
                }
            }
        }
    }

    class ProcessPack implements Callable<IPObj> {
        String pkgName;
        IPObjDao ipObjDao;
        boolean onlyMissed = false;

        public ProcessPack(String pkgName, IPObjDao ipObjDao, boolean onlyMissed) {
            this.pkgName = pkgName;
            this.ipObjDao = ipObjDao;
            this.onlyMissed = onlyMissed;
        }

        @Override
        public IPObj call() throws Exception {
            IPObj obj = new IPObj();
            IconPackUtil ip = new IconPackUtil();
            IconAttr attr = new IconAttr();
            if (!onlyMissed) {
                obj.setIconPkg(pkgName);
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(obj.getIconPkg(), PackageManager.GET_META_DATA);
                obj.setIconType("GO");
                obj.setInstallTime(pm.getPackageInfo(obj.getIconPkg(), 0).lastUpdateTime);
                obj.setIconName(mContext.getPackageManager().getApplicationLabel(ai).toString());
                obj.setTotal(ip.calcTotal(obj.getIconPkg()));
                attr.setDeleted(false);
                obj.setMissed(ip.getMissingApps(obj.getIconPkg(), Util.getInstalledApps()).size());
                attr.setSize(Util.getApkSize(obj.getIconPkg()));
                obj.setAdditional(attr.toString());
                ipObjDao.insert(obj);
                sendIntent(pkgName);
            } else {
                IPObj obj2 = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
                if (obj2.getMissed() == 0 || obj2.getAdditional() == null) {
                    attr.setDeleted(false);
                    obj2.setMissed(ip.getMissingApps(obj2.getIconPkg(), Util.getInstalledApps()).size());
                    attr.setSize(Util.getApkSize(obj2.getIconPkg()));
                    obj2.setAdditional(attr.toString());
                    ipObjDao.update(obj2);
                }
                sendIntent(pkgName);
                obj = obj2;
            }
            return obj;
        }

    }

    private void sendIntent(String pkgName) {
        Intent intentNotify = new Intent();
        intentNotify.setAction("insertlist");
        intentNotify.putExtra("pkgName", pkgName);
        mContext.sendBroadcast(intentNotify);
    }


    private void loadIconPack(List<ResolveInfo> rinfo, final IPObjDao ipObjDao, final List<IPObj> installedIconPacks) {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        Collection<Future<IPObj>> futures = new LinkedList<Future<IPObj>>();
        ArrayList<Callable<IPObj>> listCallables = new ArrayList<Callable<IPObj>>();
        for (ResolveInfo ri : rinfo) {
            final String pkgName = ri.activityInfo.packageName;
            if (!unique.contains(pkgName) && !excludePackages.contains(pkgName)) {
                unique.add(pkgName);
                final IPObj obj2 = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(pkgName)).unique();
                if (obj2 == null) {
                    listCallables.add(new ProcessPack(pkgName, ipObjDao, false));
                } else {
                    listCallables.add(new ProcessPack(pkgName, ipObjDao, true));
                }
            }
        }
        try {
            futures = executor.invokeAll(listCallables);
            for (Future<IPObj> future : futures) {
                installedIconPacks.add(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e("MICO", e.getMessage(), e);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // pool didn't terminate after the second try
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
