package dev.ukanth.iconmgr.dao;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {History.class, ApplyEvent.class}, version = 2, exportSchema = true)
public abstract class HistoryDatabase extends RoomDatabase {
    private static volatile HistoryDatabase instance;

    public abstract HistoryDao historyDao();

    /** v2 adds the apply log (History rows are untouched) */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `ApplyEvent` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packPkg` TEXT, `packName` TEXT, `launcherPkg` TEXT, `time` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ApplyEvent_time` ON `ApplyEvent` (`time`)");
        }
    };

    public static HistoryDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (HistoryDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), HistoryDatabase.class, "history-db")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return instance;
    }
}
