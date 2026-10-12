package dev.ukanth.iconmgr.dao;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One "apply pack to launcher" action started from Micopacks (launchers don't report the active
 * pack, so this is the only record of what is applied).
 */
@Entity(tableName = "ApplyEvent", indices = {@Index("time")})
public class ApplyEvent {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String packPkg;
    public String packName;
    public String launcherPkg;
    public long time;

    public ApplyEvent() {
    }

    @androidx.room.Ignore
    public ApplyEvent(String packPkg, String packName, String launcherPkg, long time) {
        this.packPkg = packPkg;
        this.packName = packName;
        this.launcherPkg = launcherPkg;
        this.time = time;
    }
}
