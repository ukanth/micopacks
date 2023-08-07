package dev.ukanth.iconmgr.dao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {History.class }, version = 1, exportSchema = false)
public abstract class Historydatabase extends RoomDatabase {
    private static Historydatabase instance;
    public abstract DaoSession Historydao();

    public static Historydatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), Historydatabase.class, "history-db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
