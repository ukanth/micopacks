package dev.ukanth.iconmgr.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import androidx.core.content.pm.PackageInfoCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;
import dev.ukanth.iconmgr.iconpack.AppFilter;
import dev.ukanth.iconmgr.iconpack.AppFilterCache;
import dev.ukanth.iconmgr.iconpack.InstalledApps;
import dev.ukanth.iconmgr.util.AppCache;
import dev.ukanth.iconmgr.util.Util;

/**
 * Single owner of the icon pack table. The UI observes {@link #observePacks()} and renders
 * whatever is stored; {@link #sync(boolean)} reconciles the table with the device in the
 * background and only does expensive work for packs that actually changed.
 */
public final class PackRepository {

    private static final String TAG = "PackRepository";
    public static final int PREVIEW_ICON_COUNT = 7;
    private static volatile PackRepository instance;

    private final Context context;
    private final IPObjDao packDao;
    private final HistoryDao historyDao;
    /** Serialises syncs and package events so they never race on the same rows. */
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();
    /** Parallel indexing of individual packs within one sync. */
    private final ExecutorService indexExecutor = Executors.newFixedThreadPool(2);

    private final MutableLiveData<Boolean> syncing = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> installedCount = new MutableLiveData<>(0);
    private final AtomicBoolean syncQueued = new AtomicBoolean(false);
    private volatile boolean forceQueued = false;

    private PackRepository(Context context) {
        this.context = context.getApplicationContext();
        this.packDao = App.getInstance().getIPObjDao();
        this.historyDao = App.getInstance().getHistoryDao();
    }

    public static PackRepository get() {
        if (instance == null) {
            synchronized (PackRepository.class) {
                if (instance == null) {
                    instance = new PackRepository(App.getContext());
                }
            }
        }
        return instance;
    }

    public LiveData<List<IPObj>> observePacks() {
        return packDao.observeAll();
    }

    public LiveData<Boolean> isSyncing() {
        return syncing;
    }

    /** Number of launcher apps on the device, used for coverage percentages. */
    public LiveData<Integer> getInstalledCount() {
        return installedCount;
    }

    /**
     * Reconcile the table with installed packs. Requests made while a sync is running are
     * coalesced into one follow-up run.
     *
     * @param force recompute every pack's stats (favorites are always kept)
     */
    public void sync(boolean force) {
        if (force) {
            forceQueued = true;
        }
        if (!syncQueued.compareAndSet(false, true)) {
            return;
        }
        syncExecutor.execute(() -> {
            syncQueued.set(false);
            boolean doForce = forceQueued;
            forceQueued = false;
            runSync(doForce);
        });
    }

    public void setFavorite(String pkg, boolean favorite) {
        App.getInstance().getDbExecutor().execute(() -> packDao.setFavorite(pkg, favorite));
    }

    /** Called for every PACKAGE_ADDED (fresh install or update). */
    public void onPackageAdded(String pkg, boolean replacing) {
        AppCache.getInstance().invalidate();
        syncExecutor.execute(() -> {
            if (!replacing && isIconPack(pkg) && !Util.getExcludedPackages().contains(pkg)
                    && packDao.getByIconPkg(pkg) == null) {
                IPObj pack = index(pkg, null, InstalledApps.current());
                if (pack != null) {
                    packDao.upsert(pack);
                    historyDao.deleteUninstalled(pkg);
                    Util.showNotification(pkg, pack.getIconName());
                }
            }
        });
        // updated packs (new versionCode) and every pack's coverage are handled by a normal sync
        sync(false);
    }

    /** Called for PACKAGE_REMOVED that is not part of an update. */
    public void onPackageRemoved(String pkg) {
        AppCache.getInstance().invalidate();
        AppFilterCache.invalidate(pkg);
        syncExecutor.execute(() -> removePack(pkg));
        sync(false);
    }

    private void runSync(boolean force) {
        syncing.postValue(true);
        long start = System.currentTimeMillis();
        try {
            if (force) {
                AppCache.getInstance().invalidate();
            }
            InstalledApps apps = InstalledApps.current();
            installedCount.postValue(apps.size());

            Set<String> installedPacks = queryInstalledPacks();
            Map<String, IPObj> stored = new HashMap<>();
            for (IPObj pack : packDao.getAll()) {
                stored.put(pack.getIconPkg(), pack);
            }

            for (String pkg : stored.keySet()) {
                if (!installedPacks.contains(pkg)) {
                    removePack(pkg);
                }
            }
            for (String pkg : installedPacks) {
                historyDao.deleteUninstalled(pkg);
            }

            PackageManager pm = context.getPackageManager();
            List<Future<IPObj>> reindexed = new ArrayList<>();
            int coverageUpdates = 0;
            for (String pkg : installedPacks) {
                IPObj current = stored.get(pkg);
                long versionCode;
                try {
                    versionCode = PackageInfoCompat.getLongVersionCode(pm.getPackageInfo(pkg, 0));
                } catch (PackageManager.NameNotFoundException e) {
                    continue;
                }
                if (force || current == null || current.getVersionCode() == 0
                        || current.getVersionCode() != versionCode) {
                    reindexed.add(indexExecutor.submit(() -> index(pkg, current, apps)));
                } else if (current.getAppsHash() != apps.getHash()) {
                    // apps were installed/removed: only coverage changed, which is a set lookup now
                    AppFilter filter = AppFilterCache.get(context, pkg);
                    packDao.updateCoverage(pkg, filter.countMissing(apps.getComponents()), apps.getHash());
                    coverageUpdates++;
                }
            }
            for (Future<IPObj> future : reindexed) {
                IPObj pack = future.get();
                if (pack != null) {
                    packDao.upsert(pack);
                }
            }
            updatePreviewIcons(apps, force || !reindexed.isEmpty() || coverageUpdates > 0);
            Log.i(TAG, "sync force=" + force + " packs=" + installedPacks.size() + " reindexed=" + reindexed.size()
                    + " coverage=" + coverageUpdates + " in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            Log.e(TAG, "sync failed", e);
        } finally {
            syncing.postValue(false);
        }
    }

    /**
     * Fills each pack's home-card strip with its icons for the same handful of apps, picked by
     * how many installed packs theme them, so cards can be compared at a glance.
     */
    private void updatePreviewIcons(InstalledApps apps, boolean changed) {
        List<IPObj> packs = packDao.getAll();
        boolean missing = false;
        for (IPObj pack : packs) {
            if (pack.getPreviewIcons() == null) {
                missing = true;
                break;
            }
        }
        if (!changed && !missing) {
            return;
        }
        Map<String, AppFilter> filters = new HashMap<>();
        Map<String, Integer> coverage = new HashMap<>();
        for (IPObj pack : packs) {
            AppFilter filter = AppFilterCache.get(context, pack.getIconPkg());
            filters.put(pack.getIconPkg(), filter);
            for (String component : apps.getComponents()) {
                if (filter.covers(component)) {
                    Integer count = coverage.get(component);
                    coverage.put(component, count == null ? 1 : count + 1);
                }
            }
        }
        List<String> order = new ArrayList<>(coverage.keySet());
        Collections.sort(order, (a, b) -> {
            int byCount = Integer.compare(coverage.get(b), coverage.get(a));
            return byCount != 0 ? byCount : a.compareTo(b);
        });
        for (IPObj pack : packs) {
            AppFilter filter = filters.get(pack.getIconPkg());
            Set<String> drawables = new LinkedHashSet<>();
            for (String component : order) {
                String drawable = filter.drawableFor(component);
                if (drawable != null) {
                    drawables.add(drawable);
                    if (drawables.size() == PREVIEW_ICON_COUNT) {
                        break;
                    }
                }
            }
            String value = String.join("|", drawables);
            if (!value.equals(pack.getPreviewIcons())) {
                packDao.updatePreviewIcons(pack.getIconPkg(), value);
            }
        }
    }

    /** Computes every stored field of a pack. Keeps user state (favorite) from {@code previous}. */
    private IPObj index(String pkg, IPObj previous, InstalledApps apps) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(pkg, 0);
            ApplicationInfo ai = info.applicationInfo != null ? info.applicationInfo : pm.getApplicationInfo(pkg, 0);
            IPObj pack = new IPObj();
            pack.setIconPkg(pkg);
            pack.setIconType("GO");
            pack.setIconName(pm.getApplicationLabel(ai).toString());
            pack.setInstallTime(info.lastUpdateTime);
            pack.setVersionCode(PackageInfoCompat.getLongVersionCode(info));
            pack.setTotal(new IconPackUtil().calcTotal(pkg));
            pack.setMissed(AppFilterCache.get(context, pkg).countMissing(apps.getComponents()));
            pack.setAppsHash(apps.getHash());
            pack.setSizeBytes(apkSizeBytes(ai));
            pack.setAuthor(Util.parseAuthorName(context, pkg));
            pack.setFavorite(previous != null && previous.isFavorite());
            return pack;
        } catch (Exception e) {
            Log.e(TAG, "index failed for " + pkg, e);
            return null;
        }
    }

    private void removePack(String pkg) {
        IPObj pack = packDao.getByIconPkg(pkg);
        if (pack == null) {
            return;
        }
        packDao.deleteByPkg(pkg);
        historyDao.insertOrReplace(new History(pack.getIconPkg(), pack.getIconName(), pack.getIconType(),
                pack.getInstallTime(), System.currentTimeMillis(), pack.getTotal(), pack.getMissed(), null));
    }

    private Set<String> queryInstalledPacks() {
        PackageManager pm = context.getPackageManager();
        Set<String> packs = new LinkedHashSet<>();
        List<String> excluded = Util.getExcludedPackages();
        for (String action : IconPackUtil.ICON_INTENTS) {
            for (ResolveInfo ri : pm.queryIntentActivities(new Intent(action), PackageManager.GET_META_DATA)) {
                String pkg = ri.activityInfo.packageName;
                if (!excluded.contains(pkg)) {
                    packs.add(pkg);
                }
            }
        }
        return packs;
    }

    private boolean isIconPack(String pkg) {
        PackageManager pm = context.getPackageManager();
        for (String action : IconPackUtil.ICON_INTENTS) {
            Intent intent = new Intent(action);
            intent.setPackage(pkg);
            if (!pm.queryIntentActivities(intent, 0).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Base APK plus split APKs, in bytes. */
    static long apkSizeBytes(ApplicationInfo ai) {
        long total = 0;
        if (ai.publicSourceDir != null) {
            total += new File(ai.publicSourceDir).length();
        }
        if (ai.splitPublicSourceDirs != null) {
            for (String split : ai.splitPublicSourceDirs) {
                total += new File(split).length();
            }
        }
        return total;
    }
}
