package dev.ukanth.iconmgr.dao;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import dev.ukanth.iconmgr.IconAttr;

@Database(entities = {IPObj.class }, version = 3, exportSchema = true)

public abstract class IPObjDatabase extends RoomDatabase {
    private static volatile IPObjDatabase instance;

    public abstract IPObjDao ipObjDao();

    /**
     * v2 moves favorite/size out of the JSON "additional" blob into columns and adds the
     * fields used for incremental re-indexing. Favorites are carried over; versionCode = 0
     * makes the next sync recompute author/size/stats for every pack.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE IPObj ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE IPObj ADD COLUMN sizeBytes INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE IPObj ADD COLUMN author TEXT");
            db.execSQL("ALTER TABLE IPObj ADD COLUMN versionCode INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE IPObj ADD COLUMN appsHash INTEGER NOT NULL DEFAULT 0");
            try (Cursor c = db.query("SELECT iconPkg, additional FROM IPObj")) {
                while (c.moveToNext()) {
                    if (IconAttr.fromJson(c.getString(1)).isFavorite()) {
                        db.execSQL("UPDATE IPObj SET favorite = 1 WHERE iconPkg = ?", new Object[]{c.getString(0)});
                    }
                }
            }
        }
    };

    /** v3 adds the home card's preview icon list (computed by the next sync). */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE IPObj ADD COLUMN previewIcons TEXT");
        }
    };

    public static IPObjDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (IPObjDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), IPObjDatabase.class, "icons-db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            // only an app downgrade may wipe; upgrades must migrate
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return instance;
    }
}
