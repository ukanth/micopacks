package dev.ukanth.iconmgr.util;

import android.content.Context;

import com.google.gson.Gson;

import java.util.Comparator;

import dev.ukanth.iconmgr.IconAttr;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.dao.IPObj;

/**
 * Created by ukanth on 24/7/17.
 */

public class PackageComparator implements Comparator<IPObj> {

    private Context context;

    public PackageComparator(Context context) {
        this.context = context;
    }

    @Override
    public int compare( IPObj o1, IPObj o2) {
        switch (Prefs.sortBy()) {
            case "s0":
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getIconName(), o2.getIconName());
            case "s1":
                return (o1.getInstallTime() > o2.getInstallTime()) ? -1 : (o1.getInstallTime() < o2.getInstallTime()) ? 1 : 0;
            case "s2":
                return (o1.getTotal() > o2.getTotal()) ? -1 : (o1.getTotal() < o2.getTotal()) ? 1 : 0;
            case "s3":
                long o1Attr = o1.getAdditional() != null ? new Gson().fromJson(o1.getAdditional(), IconAttr.class).getSize() : 0;
                long o2Attr = o2.getAdditional() != null ? new Gson().fromJson(o2.getAdditional(), IconAttr.class).getSize() : 0;
                return (o1Attr > o2Attr) ? -1 : (o1Attr < o2Attr) ? 1 : 0;
            case "s4":
                return (o2.getMissed() > o1.getMissed()) ? -1 : (o2.getMissed() < o1.getMissed()) ? 1 : 0;
            case "s5":
                String authorName1 = Util.getAuthorName(context, o1.getIconPkg());
                String authorName2 = Util.getAuthorName(context, o2.getIconPkg());
                return String.CASE_INSENSITIVE_ORDER.compare(authorName1, authorName2);
        }
        return 1;
    }
}
