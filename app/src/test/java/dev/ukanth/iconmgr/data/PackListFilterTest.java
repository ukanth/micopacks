package dev.ukanth.iconmgr.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.util.PackageComparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PackListFilterTest {

    static IPObj pack(String pkg, String name, String author, int total, int missed, long size, long updated, boolean fav) {
        IPObj p = new IPObj();
        p.setIconPkg(pkg);
        p.setIconName(name);
        p.setAuthor(author);
        p.setTotal(total);
        p.setMissed(missed);
        p.setSizeBytes(size);
        p.setInstallTime(updated);
        p.setFavorite(fav);
        return p;
    }

    private final IPObj delta = pack("website.leifs.delta", "Delta", "Leif Niemczik", 14381, 25, 68, 300, false);
    private final IPObj lines = pack("com.natewren.linesfree", "Lines Free", "Nate Wren", 4322, 51, 59, 100, true);
    private final IPObj nexa = pack("com.jndapp.square.nexa.iconpack", "Nexa", null, 6931, 41, 83, 200, false);

    private List<String> names(List<IPObj> packs) {
        List<String> names = new ArrayList<>();
        for (IPObj p : packs) names.add(p.getIconName());
        return names;
    }

    private List<IPObj> all() {
        return Arrays.asList(nexa, delta, lines);
    }

    @Test
    public void noMatchGivesEmptyList() {
        // the old filter fell back to showing every pack
        assertTrue(PackListFilter.apply(all(), "zzz", PackageComparator.BY_NAME, false).isEmpty());
    }

    @Test
    public void matchesNameAuthorAndPackageCaseInsensitively() {
        assertEquals(Collections.singletonList("Delta"), names(PackListFilter.apply(all(), "DEL", PackageComparator.BY_NAME, false)));
        assertEquals(Collections.singletonList("Lines Free"), names(PackListFilter.apply(all(), "wren", PackageComparator.BY_NAME, false)));
        assertEquals(Collections.singletonList("Nexa"), names(PackListFilter.apply(all(), "jndapp", PackageComparator.BY_NAME, false)));
    }

    @Test
    public void blankQueryShowsAll() {
        assertEquals(3, PackListFilter.apply(all(), "  ", PackageComparator.BY_NAME, false).size());
        assertEquals(3, PackListFilter.apply(all(), null, PackageComparator.BY_NAME, false).size());
    }

    @Test
    public void sortKeys() {
        assertEquals(Arrays.asList("Delta", "Lines Free", "Nexa"), names(PackListFilter.apply(all(), "", PackageComparator.BY_NAME, false)));
        assertEquals(Arrays.asList("Delta", "Nexa", "Lines Free"), names(PackListFilter.apply(all(), "", PackageComparator.BY_LAST_UPDATE, false)));
        assertEquals(Arrays.asList("Delta", "Nexa", "Lines Free"), names(PackListFilter.apply(all(), "", PackageComparator.BY_ICON_COUNT, false)));
        assertEquals(Arrays.asList("Nexa", "Delta", "Lines Free"), names(PackListFilter.apply(all(), "", PackageComparator.BY_SIZE, false)));
        // fewest missing apps (best coverage) first
        assertEquals(Arrays.asList("Delta", "Nexa", "Lines Free"), names(PackListFilter.apply(all(), "", PackageComparator.BY_COVERAGE, false)));
    }

    @Test
    public void authorSortPutsMissingAuthorsLast() {
        assertEquals(Arrays.asList("Delta", "Lines Free", "Nexa"), names(PackListFilter.apply(all(), "", PackageComparator.BY_AUTHOR, false)));
    }

    @Test
    public void tiesBreakByName() {
        IPObj a = pack("a", "Alpha", "Android", 1, 0, 0, 0, false);
        IPObj b = pack("b", "beta", "Android", 1, 0, 0, 0, false);
        IPObj c = pack("c", "Charlie", "Android", 1, 0, 0, 0, false);
        assertEquals(Arrays.asList("Alpha", "beta", "Charlie"),
                names(PackListFilter.apply(Arrays.asList(c, b, a), "", PackageComparator.BY_AUTHOR, false)));
    }

    @Test
    public void favoritesFirstKeepsSortWithinGroups() {
        IPObj fav2 = pack("x", "Arcticons", "Donnnno", 1, 0, 0, 0, true);
        List<IPObj> sorted = PackListFilter.apply(Arrays.asList(nexa, delta, lines, fav2), "", PackageComparator.BY_NAME, true);
        assertEquals(Arrays.asList("Arcticons", "Lines Free", "Delta", "Nexa"), names(sorted));
    }

    @Test
    public void unknownSortKeyFallsBackToName() {
        assertEquals(Arrays.asList("Delta", "Lines Free", "Nexa"), names(PackListFilter.apply(all(), "", "bogus", false)));
        assertEquals(Arrays.asList("Delta", "Lines Free", "Nexa"), names(PackListFilter.apply(all(), "", null, false)));
    }

    @Test
    public void nullListIsEmpty() {
        assertTrue(PackListFilter.apply(null, "", PackageComparator.BY_NAME, false).isEmpty());
    }

    @Test
    public void favoritesScopeShowsOnlyFavorites() {
        List<IPObj> shown = PackListFilter.apply(all(), "", PackageComparator.BY_NAME, false, PackListFilter.Scope.FAVORITES, 1000);
        assertEquals(Collections.singletonList("Lines Free"), names(shown));
    }

    @Test
    public void recentScopeUsesThirtyDayWindow() {
        // updated at 300 (Delta) is 30 days - 50ms old: inside; 200 (Nexa) and 100 (Lines) are older
        long now = PackListFilter.RECENT_WINDOW_MS + 250;
        List<IPObj> shown = PackListFilter.apply(all(), "", PackageComparator.BY_NAME, false, PackListFilter.Scope.RECENT, now);
        assertEquals(Collections.singletonList("Delta"), names(shown));
    }
}
