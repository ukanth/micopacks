package dev.ukanth.iconmgr.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

@Dao
public interface IPObjDao {

    String QUERY_BY_ICON_PKG = "SELECT * FROM IPObj WHERE iconPkg = :pkgName";

    @Query(QUERY_BY_ICON_PKG)
    IPObj getByIconPkg(String pkgName);

    @Query(QUERY_BY_ICON_PKG)
    LiveData<IPObj> observeByIconPkg(String pkgName);

    @Query("SELECT * FROM IPObj ORDER BY iconName ASC")
    List<IPObj> getAllOrderedByIconName();


    @Query("SELECT * FROM IPObj")
    List<IPObj> getAll();

    /** Emits again whenever the table changes; the main list renders straight from this. */
    @Query("SELECT * FROM IPObj")
    LiveData<List<IPObj>> observeAll();

    @Query("SELECT * FROM IPObj WHERE favorite = 1 ORDER BY iconName ASC")
    List<IPObj> getFavorites();

    @Query("SELECT * FROM IPObj WHERE iconPkg IN (:packageIds)")
    List<IPObj> loadAllByIds(String[] packageIds);

    @Insert
    void insertAll(IPObj... ipObjs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(IPObj ipObj);

    @Delete
    void delete(IPObj user);

    @Query("DELETE FROM IPObj WHERE iconPkg = :pkgName")
    void deleteByPkg(String pkgName);

    @Query("DELETE FROM IPObj")
    void deleteAll();

    @Update
    void  update(IPObj ipObj);

    @Query("UPDATE IPObj SET favorite = :favorite WHERE iconPkg = :pkgName")
    void setFavorite(String pkgName, boolean favorite);

    @Query("UPDATE IPObj SET previewIcons = :previewIcons WHERE iconPkg = :pkgName")
    void updatePreviewIcons(String pkgName, String previewIcons);

    @Query("UPDATE IPObj SET missed = :missed, appsHash = :appsHash WHERE iconPkg = :pkgName")
    void updateCoverage(String pkgName, int missed, int appsHash);
}
