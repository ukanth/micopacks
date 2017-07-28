package dev.ukanth.iconmgr.util;

import android.content.Context;

import java.util.Comparator;

import dev.ukanth.iconmgr.IconPack;
import dev.ukanth.iconmgr.Prefs;

/**
 * Created by ukanth on 24/7/17.
 */

public class PackageComparator implements Comparator<IconPack> {

    public PackageComparator setCtx(Context ctx) {
        this.ctx = ctx;
        return this;
    }


    private Context ctx;

    @Override
    public int compare(IconPack o1, IconPack o2) {
        switch (Prefs.sortBy(ctx)) {
            case "s0":
                return String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name);
            case "s1":
                return (o1.installTime > o2.installTime) ? -1: (o1.installTime < o2.installTime) ? 1 : 0;
            case "s2":
                return (o1.getCount() > o2.getCount()) ? -1: (o1.getCount() < o2.getCount()) ? 1 : 0;
            case "s3":
                return (o1.getMatch() > o2.getMatch()) ? -1: (o1.getMatch() < o2.getMatch()) ? 1 : 0;
        }
        return 1;
    }
}
