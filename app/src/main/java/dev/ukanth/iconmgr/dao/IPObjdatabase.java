package dev.ukanth.iconmgr.dao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {IPObj.class }, version = 1, exportSchema = false)
public abstract class IPObjdatabase extends RoomDatabase {
    private static IPObjdatabase instance;
   public abstract DaoSession IPObjdao();

    public static IPObjdatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), IPObjdatabase.class, "icons-db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
