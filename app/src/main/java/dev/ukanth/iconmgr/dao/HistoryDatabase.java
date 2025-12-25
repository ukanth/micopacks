package dev.ukanth.iconmgr.dao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {History.class }, version = 1, exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {
    private static HistoryDatabase instance;

    public abstract HistoryDao historyDao();

    public static HistoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), HistoryDatabase.class, "history-db")
                    .build();
        }
        return instance;
    }
}
