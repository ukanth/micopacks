package dev.ukanth.iconmgr.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

@Dao
public interface IPObjDao {

    String QUERY_BY_ICON_PKG = "SELECT * FROM IPObj WHERE iconPkg = :pkgName";

    @Query(QUERY_BY_ICON_PKG)
    IPObj getByIconPkg(String pkgName);

    @Query("SELECT * FROM IPObj ORDER BY iconName ASC")
    List<IPObj> getAllOrderedByIconName();


    @Query("SELECT * FROM IPObj")
    List<IPObj> getAll();

    @Query("SELECT * FROM IPObj WHERE iconPkg IN (:packageIds)")
    List<IPObj> loadAllByIds(String[] packageIds);

    @Insert
    void insertAll(IPObj... ipObjs);

    @Delete
    void delete(IPObj user);

    @Query("DELETE FROM IPObj")
    void deleteAll();

    @Update
    void  update(IPObj ipObj);


}