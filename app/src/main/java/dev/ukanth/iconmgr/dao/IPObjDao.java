package dev.ukanth.iconmgr.dao;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface IPObjDao {

    @Query("SELECT * FROM IPObj")
    List<IPObj> getAll();

    @Query("SELECT * FROM IPObj WHERE iconPkg IN (:packageIds)")
    List<IPObj> loadAllByIds(String[] packageIds);

    @Insert
    void insertAll(IPObj... ipObjs);

    @Delete
    void delete(IPObj user);

}
