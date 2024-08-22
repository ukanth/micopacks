package dev.ukanth.iconmgr.dao;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface FreezeDao  {

    @Insert
    void insertIntoFreezeTable(Freeze freeze);





}
