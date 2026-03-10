package com.mosleemapp.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prayer_tracker")
public class PrayerTrackerEntity {
    @PrimaryKey
    @NonNull
    public String date; // Format: YYYY-MM-DD

    public boolean fajr;
    public boolean dhuhr;
    public boolean asr;
    public boolean maghrib;
    public boolean isha;
    
    public boolean tilawah;
    public boolean tahajud;
    public boolean duha;
    public boolean fast;

    public PrayerTrackerEntity(@NonNull String date) {
        this.date = date;
    }
}
