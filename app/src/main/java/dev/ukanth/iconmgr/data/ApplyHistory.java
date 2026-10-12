package dev.ukanth.iconmgr.data;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.dao.ApplyEvent;
import dev.ukanth.iconmgr.dao.HistoryDao;
import dev.ukanth.iconmgr.dao.IPObj;

/**
 * Records every apply Micopacks starts. Launchers don't report which pack is active, so the
 * home "Last applied" card and the History tab are built from this log.
 */
public final class ApplyHistory {

    private static final int KEEP = 200;

    private ApplyHistory() {
    }

    public static void record(String packPkg, String launcherPkg) {
        if (packPkg == null) {
            return;
        }
        final long now = System.currentTimeMillis();
        App.getInstance().getDbExecutor().execute(() -> {
            IPObj pack = App.getInstance().getIPObjDao().getByIconPkg(packPkg);
            HistoryDao dao = App.getInstance().getHistoryDao();
            dao.insertApply(new ApplyEvent(packPkg, pack != null ? pack.getIconName() : packPkg, launcherPkg, now));
            dao.trimApplies(KEEP);
        });
    }
}
