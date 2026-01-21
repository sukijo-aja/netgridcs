package com.mosleemapp.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PrayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPrayerTimes(List<PrayerTimeEntity> prayerTimes);

    @Query("SELECT * FROM prayer_times WHERE date = :date LIMIT 1")
    PrayerTimeEntity getPrayerTime(String date);

    @Query("DELETE FROM prayer_times")
    void clearAll();
}
