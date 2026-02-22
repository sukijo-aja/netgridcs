package com.mosleemapp.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosleemapp.app.data.local.entity.CustomHabitEntity;
import com.mosleemapp.app.data.local.entity.CustomHabitLogEntity;

import java.util.List;

@Dao
public interface CustomHabitDao {
    @Insert
    void insertHabit(CustomHabitEntity habit);

    @Delete
    void deleteHabit(CustomHabitEntity habit);

    @Query("SELECT * FROM custom_habits ORDER BY id ASC")
    LiveData<List<CustomHabitEntity>> getAllHabits();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateLog(CustomHabitLogEntity log);

    @Query("SELECT * FROM custom_habit_logs WHERE date = :date")
    LiveData<List<CustomHabitLogEntity>> getLogsForDate(String date);
}
