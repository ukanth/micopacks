package dev.ukanth.iconmgr.dao;


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

}
