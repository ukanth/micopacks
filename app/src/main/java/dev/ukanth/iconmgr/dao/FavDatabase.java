package dev.ukanth.iconmgr.dao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Favorite.class }, version = 1, exportSchema = false)
public abstract class FavDatabase extends RoomDatabase {
    private static FavDatabase instance;

    public abstract FavDao favDao();

    public static FavDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), FavDatabase.class, "fav-db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}

