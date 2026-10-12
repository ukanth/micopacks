package dev.ukanth.iconmgr.util;

import java.util.Comparator;

import dev.ukanth.iconmgr.dao.IPObj;

/**
 * Created by ukanth on 24/7/17.
 *
 * Sorts packs by one of the menu's sort keys. Works only on stored columns, so it does no
 * I/O and is safe to run on the main thread.
 */

public class PackageComparator implements Comparator<IPObj> {

    public static final String BY_NAME = "s0";
    public static final String BY_LAST_UPDATE = "s1";
    public static final String BY_ICON_COUNT = "s2";
    public static final String BY_SIZE = "s3";
    public static final String BY_COVERAGE = "s4";
    public static final String BY_AUTHOR = "s5";

    private final String sortKey;

    public PackageComparator(String sortKey) {
        this.sortKey = sortKey != null ? sortKey : BY_NAME;
    }

    @Override
    public int compare( IPObj o1, IPObj o2) {
        int result;
        switch (sortKey) {
            case BY_LAST_UPDATE:
                result = Long.compare(o2.getInstallTime(), o1.getInstallTime());
                break;
            case BY_ICON_COUNT:
                result = Integer.compare(o2.getTotal(), o1.getTotal());
                break;
            case BY_SIZE:
                result = Long.compare(o2.getSizeBytes(), o1.getSizeBytes());
                break;
            case BY_COVERAGE:
                // fewer missing apps = better coverage, first
                result = Integer.compare(o1.getMissed(), o2.getMissed());
                break;
            case BY_AUTHOR:
                result = compareNullsLast(o1.getAuthor(), o2.getAuthor());
                break;
            case BY_NAME:
            default:
                result = 0;
        }
        // name breaks ties so the order is stable and predictable
        return result != 0 ? result : compareNullsLast(o1.getIconName(), o2.getIconName());
    }

    // packs without a certificate CN (or label) sort after named ones instead of throwing
    static int compareNullsLast(String a, String b) {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return String.CASE_INSENSITIVE_ORDER.compare(a, b);
    }
}
