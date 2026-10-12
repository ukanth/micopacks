package dev.ukanth.iconmgr.detail;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.iconpack.AppFilter;
import dev.ukanth.iconmgr.iconpack.AppFilterCache;
import dev.ukanth.iconmgr.util.Util;

/**
 * State of the pack detail screen: the stored pack row plus the four tab lists, built once in the
 * background from the cached appfilter (names only; the grid loads bitmaps on demand).
 */
public class PackDetailViewModel extends ViewModel {

    public enum Tab { YOUR_APPS, MISSING, ALL, MASKS }

    private static final ExecutorService loader = Executors.newSingleThreadExecutor();
    private static final Comparator<IconItem> BY_TITLE = (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.title, b.title);

    private String packageName;
    private LiveData<IPObj> pack;
    private final MutableLiveData<Map<Tab, List<IconItem>>> lists = new MutableLiveData<>();
    private final MutableLiveData<Integer> installedCount = new MutableLiveData<>();

    public void init(String packageName) {
        if (this.packageName != null) {
            return;
        }
        this.packageName = packageName;
        this.pack = App.getInstance().getIPObjDao().observeByIconPkg(packageName);
        loader.execute(this::load);
    }

    public String getPackageName() {
        return packageName;
    }

    public LiveData<IPObj> getPack() {
        return pack;
    }

    /** All tab lists at once; null until loaded. */
    public LiveData<Map<Tab, List<IconItem>>> getLists() {
        return lists;
    }

    public LiveData<Integer> getInstalledCount() {
        return installedCount;
    }

    private void load() {
        long start = System.currentTimeMillis();
        Map<Tab, List<IconItem>> result = new EnumMap<>(Tab.class);
        try {
            Context context = App.getContext();
            PackageManager pm = context.getPackageManager();
            AppFilter filter = AppFilterCache.get(context, packageName);
            List<ResolveInfo> apps = Util.getInstalledApps();
            installedCount.postValue(apps.size());

            List<IconItem> yours = new ArrayList<>();
            List<IconItem> missing = new ArrayList<>();
            for (ResolveInfo app : apps) {
                String appPackage = app.activityInfo.packageName;
                String component = appPackage + "/" + app.activityInfo.name;
                String label = String.valueOf(app.loadLabel(pm));
                String drawable = filter.drawableFor(component);
                if (drawable != null) {
                    yours.add(IconItem.packIcon(label, drawable, appPackage));
                } else {
                    missing.add(IconItem.missingApp(label, appPackage));
                }
            }
            Collections.sort(yours, BY_TITLE);
            Collections.sort(missing, BY_TITLE);

            // full catalogue: appfilter drawables plus drawable.xml (often much larger)
            Set<String> names = new LinkedHashSet<>(filter.getComponentToDrawable().values());
            names.addAll(new IconPackUtil().getDrawableXmlNames(packageName));
            List<IconItem> all = new ArrayList<>(names.size());
            for (String name : names) {
                all.add(IconItem.packIcon(name, name, null));
            }
            Collections.sort(all, BY_TITLE);

            List<IconItem> masks = new ArrayList<>();
            for (String name : filter.getBackNames()) {
                masks.add(IconItem.packIcon(name, name, null));
            }
            if (filter.getMaskName() != null) masks.add(IconItem.packIcon(filter.getMaskName(), filter.getMaskName(), null));
            if (filter.getUponName() != null) masks.add(IconItem.packIcon(filter.getUponName(), filter.getUponName(), null));

            result.put(Tab.YOUR_APPS, yours);
            result.put(Tab.MISSING, missing);
            result.put(Tab.ALL, all);
            result.put(Tab.MASKS, masks);
            Log.i("PackDetail", packageName + ": yours=" + yours.size() + " missing=" + missing.size()
                    + " all=" + all.size() + " masks=" + masks.size() + " in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            Log.e("PackDetail", "Loading " + packageName + " failed", e);
            for (Tab tab : Tab.values()) {
                result.put(tab, Collections.emptyList());
            }
        }
        lists.postValue(result);
    }

    /** Case-insensitive title filter for the search box. */
    public static List<IconItem> filter(List<IconItem> items, String query) {
        if (items == null) {
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return items;
        }
        String q = query.trim().toLowerCase();
        List<IconItem> out = new ArrayList<>();
        for (IconItem item : items) {
            if (item.title.toLowerCase().contains(q)
                    || (item.drawable != null && item.drawable.toLowerCase().contains(q))) {
                out.add(item);
            }
        }
        return out;
    }
}
