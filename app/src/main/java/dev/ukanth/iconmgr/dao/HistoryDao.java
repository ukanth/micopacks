package dev.ukanth.iconmgr.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM History")
    List<History> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(History history);

    /** Uninstalled packs, newest first */
    @Query("SELECT * FROM History ORDER BY uninstallTime DESC")
    LiveData<List<History>> observeUninstalled();

    /** A reinstalled pack is no longer "uninstalled" */
    @Query("DELETE FROM History WHERE iconPkg = :pkgName")
    void deleteUninstalled(String pkgName);

    @Insert
    void insertApply(ApplyEvent event);

    @Query("SELECT * FROM ApplyEvent ORDER BY time DESC LIMIT :limit")
    LiveData<List<ApplyEvent>> observeApplies(int limit);

    @Query("SELECT * FROM ApplyEvent ORDER BY time DESC LIMIT :limit")
    List<ApplyEvent> getRecentApplies(int limit);

    /** keep the log bounded */
    @Query("DELETE FROM ApplyEvent WHERE id NOT IN (SELECT id FROM ApplyEvent ORDER BY time DESC LIMIT :keep)")
    void trimApplies(int keep);
}
