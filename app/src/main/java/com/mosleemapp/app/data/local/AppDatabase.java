package com.mosleemapp.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mosleemapp.app.data.local.dao.HadithDao;
import com.mosleemapp.app.data.local.dao.QuranDao;
import com.mosleemapp.app.data.local.dao.DuaDao;
import com.mosleemapp.app.data.local.entity.AyahEntity;
import com.mosleemapp.app.data.local.entity.HadithBookEntity;
import com.mosleemapp.app.data.local.entity.HadithEntity;
import com.mosleemapp.app.data.local.entity.SurahEntity;
import com.mosleemapp.app.data.local.entity.DuaEntity;

@Database(entities = { PrayerTimeEntity.class, SurahEntity.class, AyahEntity.class, HadithBookEntity.class, HadithEntity.class, DuaEntity.class }, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PrayerDao prayerDao();
    public abstract QuranDao quranDao();
    public abstract HadithDao hadithDao();
    public abstract DuaDao duaDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "moslem_app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
