package dev.ukanth.iconmgr.iconpack;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class InstalledAppsTest {

    @Test
    public void hashIgnoresOrderAndDuplicates() {
        InstalledApps a = new InstalledApps(Arrays.asList("x/x.A", "y/y.B", "z/z.C"));
        InstalledApps b = new InstalledApps(Arrays.asList("z/z.C", "x/x.A", "y/y.B", "x/x.A"));
        assertEquals(a.getHash(), b.getHash());
        assertEquals(3, b.size());
    }

    @Test
    public void hashChangesWhenAnAppIsAddedOrRemoved() {
        InstalledApps base = new InstalledApps(Arrays.asList("x/x.A", "y/y.B"));
        assertNotEquals(base.getHash(), new InstalledApps(Arrays.asList("x/x.A", "y/y.B", "z/z.C")).getHash());
        assertNotEquals(base.getHash(), new InstalledApps(Arrays.asList("x/x.A")).getHash());
    }

    @Test
    public void coveragePercent() {
        assertEquals(76, InstalledApps.coveragePercent(103, 25));   // 78/103 = 75.7%
        assertEquals(100, InstalledApps.coveragePercent(10, 0));
        assertEquals(0, InstalledApps.coveragePercent(10, 10));
    }

    @Test
    public void coveragePercentEdgeCases() {
        assertEquals(0, InstalledApps.coveragePercent(0, 0));     // no apps: no division by zero
        assertEquals(0, InstalledApps.coveragePercent(10, 15));   // stale missed > installed
    }
}
