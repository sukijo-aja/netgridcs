package com.androidstarter.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.androidstarter.app.data.local.dao.ExampleDao;
import com.androidstarter.app.data.local.entity.ExampleEntity;

@Database(entities = { ExampleEntity.class }, version = 17, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExampleDao exampleDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "android_starter")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
