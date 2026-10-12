package dev.ukanth.iconmgr.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.detail.IconItem;
import dev.ukanth.iconmgr.iconpack.AppFilterCache;

/**
 * Icons tab: searches the icon names of every installed pack. Each pack's catalogue (appfilter
 * plus drawable.xml) is read once; searches then run in memory.
 */
public class IconsViewModel extends ViewModel {

    public static final int MAX_PER_PACK = 120;
    public static final int MAX_TOTAL = 600;

    public static final class Results {
        public final String query;
        public final List<IconItem> items;

        Results(String query, List<IconItem> items) {
            this.query = query;
            this.items = items;
        }
    }

    private static final ExecutorService worker = Executors.newSingleThreadExecutor();

    /** pack package -> its icon names (lower-cased copy kept alongside for matching) */
    private final Map<String, List<String>> catalogues = new LinkedHashMap<>();
    private final Map<String, String> packNames = new HashMap<>();
    private final MutableLiveData<Results> results = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final AtomicInteger generation = new AtomicInteger();
    private volatile boolean loaded;

    public LiveData<Results> getResults() {
        return results;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public String packName(String pkg) {
        String name = packNames.get(pkg);
        return name != null ? name : pkg;
    }

    public int packCount() {
        return catalogues.size();
    }

    /** Runs the search in the background; an older search finishing late is discarded. */
    public void search(String rawQuery) {
        final String query = rawQuery == null ? "" : rawQuery.trim();
        final int gen = generation.incrementAndGet();
        if (query.isEmpty()) {
            results.setValue(new Results("", new ArrayList<>()));
            return;
        }
        loading.setValue(!loaded);
        worker.execute(() -> {
            ensureLoaded();
            List<IconItem> found = match(query.toLowerCase(Locale.ROOT));
            if (gen == generation.get()) {
                loading.postValue(false);
                results.postValue(new Results(query, found));
            }
        });
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        long start = System.currentTimeMillis();
        IconPackUtil util = new IconPackUtil();
        for (IPObj pack : App.getInstance().getIPObjDao().getAllOrderedByIconName()) {
            Set<String> names = new LinkedHashSet<>(AppFilterCache.get(App.getContext(), pack.getIconPkg())
                    .getComponentToDrawable().values());
            names.addAll(util.getDrawableXmlNames(pack.getIconPkg()));
            catalogues.put(pack.getIconPkg(), new ArrayList<>(names));
            packNames.put(pack.getIconPkg(), pack.getIconName());
        }
        loaded = true;
        Log.i("IconsSearch", "catalogues for " + catalogues.size() + " packs in " + (System.currentTimeMillis() - start) + "ms");
    }

    private List<IconItem> match(String q) {
        List<IconItem> found = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : catalogues.entrySet()) {
            int perPack = 0;
            for (String name : entry.getValue()) {
                if (name.toLowerCase(Locale.ROOT).contains(q)) {
                    found.add(IconItem.fromPack(entry.getKey(), name));
                    if (++perPack >= MAX_PER_PACK || found.size() >= MAX_TOTAL) {
                        break;
                    }
                }
            }
            if (found.size() >= MAX_TOTAL) {
                break;
            }
        }
        return found;
    }
}
