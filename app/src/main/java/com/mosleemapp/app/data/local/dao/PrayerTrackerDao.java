package com.mosleemapp.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosleemapp.app.data.local.entity.PrayerTrackerEntity;

import java.util.List;

@Dao
public interface PrayerTrackerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(PrayerTrackerEntity entity);

    @Query("SELECT * FROM prayer_tracker WHERE date = :date")
    LiveData<PrayerTrackerEntity> getTrackerForDate(String date);

    @Query("SELECT * FROM prayer_tracker")
    LiveData<List<PrayerTrackerEntity>> getAllTrackers();
}
