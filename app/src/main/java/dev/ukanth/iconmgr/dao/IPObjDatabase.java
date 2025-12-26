package dev.ukanth.iconmgr.dao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {IPObj.class }, version = 1, exportSchema = false)

public abstract class IPObjDatabase extends RoomDatabase {
    private static IPObjDatabase instance;

    public abstract IPObjDao ipObjDao();

    public static IPObjDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), IPObjDatabase.class, "icons-db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
