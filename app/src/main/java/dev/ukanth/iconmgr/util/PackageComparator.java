package dev.ukanth.iconmgr.util;

import android.content.Context;

import java.util.Comparator;

import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.dao.IPObj;

/**
 * Created by ukanth on 24/7/17.
 */

public class PackageComparator implements Comparator<IPObj> {

    public PackageComparator setCtx(Context ctx) {
        this.ctx = ctx;
        return this;
    }


    private Context ctx;

    @Override
    public int compare(IPObj o1, IPObj o2) {
        switch (Prefs.sortBy(ctx)) {
            case "s0":
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getIconName(), o2.getIconName());
            case "s1":
                return (o1.getInstallTime() > o2.getInstallTime()) ? -1: (o1.getInstallTime() < o2.getInstallTime()) ? 1 : 0;
            case "s2":
                return (o1.getTotal() > o2.getTotal()) ? -1: (o1.getTotal() < o2.getTotal()) ? 1 : 0;
            /*case "s3":
                return (o1.getMatch() > o2.getMatch()) ? -1: (o1.getMatch() < o2.getMatch()) ? 1 : 0;*/
        }
        return 1;
    }
}
