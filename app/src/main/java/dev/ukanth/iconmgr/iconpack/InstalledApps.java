package dev.ukanth.iconmgr.iconpack;

import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ukanth.iconmgr.util.Util;

/**
 * Snapshot of the launcher activities on the device, in the "pkg/Activity" form appfilters use.
 */
public final class InstalledApps {

    private final Set<String> components;
    private final int hash;

    public InstalledApps(Collection<String> components) {
        this.components = Collections.unmodifiableSet(new HashSet<>(components));
        this.hash = hashOf(this.components);
    }

    public static InstalledApps current() {
        List<ResolveInfo> apps = Util.getInstalledApps();
        List<String> components = new ArrayList<>(apps.size());
        for (ResolveInfo app : apps) {
            components.add(app.activityInfo.packageName + "/" + app.activityInfo.name);
        }
        return new InstalledApps(components);
    }

    public Set<String> getComponents() {
        return components;
    }

    public int size() {
        return components.size();
    }

    /** Order-independent fingerprint; when it changes, every pack's coverage must be recomputed. */
    public int getHash() {
        return hash;
    }

    static int hashOf(Set<String> components) {
        List<String> sorted = new ArrayList<>(components);
        Collections.sort(sorted);
        return sorted.hashCode();
    }

    /** Percentage of installed apps a pack themes, 0-100. */
    public static int coveragePercent(int installed, int missed) {
        if (installed <= 0) {
            return 0;
        }
        int themed = Math.max(0, installed - missed);
        return (int) Math.round(themed * 100.0 / installed);
    }
}
