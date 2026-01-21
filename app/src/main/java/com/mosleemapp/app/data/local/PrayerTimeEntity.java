package com.mosleemapp.app.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prayer_times")
public class PrayerTimeEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date; // YYYY-MM-DD
    public String fajr;
    public String dhuhr;
    public String asr;
    public String maghrib;
    public String isha;

    public PrayerTimeEntity(String date, String fajr, String dhuhr, String asr, String maghrib, String isha) {
        this.date = date;
        this.fajr = fajr;
        this.dhuhr = dhuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.isha = isha;
    }
}
