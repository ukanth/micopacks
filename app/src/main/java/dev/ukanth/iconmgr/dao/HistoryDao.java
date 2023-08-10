//package dev.ukanth.iconmgr.dao;
//
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import java.util.List;
//
//@Dao
//public interface HistoryDao {
//
//    @Query("SELECT * FROM History")
//    List<History> getAll();
//
//    @Query("SELECT * FROM History WHERE iconPkg IN (:packageIds)")
//    List<History> loadAllByIds(String[] packageIds);
//
//    @Insert
//    void insertAll(History... histories);
//
//    @Delete
//    void delete(History user);
//}
