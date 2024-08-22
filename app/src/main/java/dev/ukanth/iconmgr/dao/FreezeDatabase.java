package dev.ukanth.iconmgr.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Freeze.class }, version = 1, exportSchema = false)
public abstract class FreezeDatabase extends RoomDatabase {
    private static FreezeDatabase instance;

    public abstract FreezeDao freezeDao();

    public static FreezeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), FreezeDatabase.class, "Freeze-db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }




}
