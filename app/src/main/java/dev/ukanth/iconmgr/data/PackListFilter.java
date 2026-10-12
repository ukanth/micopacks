package dev.ukanth.iconmgr.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.util.PackageComparator;

/**
 * What the main list shows for a given search query and sort. Pure, so it is unit tested.
 */
public final class PackListFilter {

    /** Home filter chips */
    public enum Scope { ALL, FAVORITES, RECENT }

    /** "Updated recently" = pack updated within this window */
    public static final long RECENT_WINDOW_MS = 30L * 24 * 60 * 60 * 1000;

    private PackListFilter() {
    }

    public static List<IPObj> apply(List<IPObj> packs, String query, String sortKey, boolean favoritesFirst) {
        return apply(packs, query, sortKey, favoritesFirst, Scope.ALL, System.currentTimeMillis());
    }

    public static List<IPObj> apply(List<IPObj> packs, String query, String sortKey, boolean favoritesFirst,
                                    Scope scope, long now) {
        if (packs == null) {
            return Collections.emptyList();
        }
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<IPObj> result = new ArrayList<>();
        for (IPObj pack : packs) {
            if (scope == Scope.FAVORITES && !pack.isFavorite()) continue;
            if (scope == Scope.RECENT && now - pack.getInstallTime() > RECENT_WINDOW_MS) continue;
            if (q.isEmpty() || matches(pack, q)) {
                result.add(pack);
            }
        }
        Comparator<IPObj> order = new PackageComparator(sortKey);
        if (favoritesFirst) {
            Comparator<IPObj> favorites = (a, b) -> Boolean.compare(b.isFavorite(), a.isFavorite());
            order = favorites.thenComparing(order);
        }
        Collections.sort(result, order);
        // no match means an empty list (the old filter fell back to showing every pack)
        return result;
    }

    private static boolean matches(IPObj pack, String q) {
        return contains(pack.getIconName(), q) || contains(pack.getAuthor(), q) || contains(pack.getIconPkg(), q);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
