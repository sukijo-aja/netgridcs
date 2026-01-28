package com.mosleemapp.app.data.local;

public class DefaultPrayerData {
    public static PrayerTimeEntity getDefault(String date) {
        return new PrayerTimeEntity(
                date,
                "04:30", // Fajr
                "12:00", // Dhuhr
                "15:30", // Asr
                "18:00", // Maghrib
                "19:30", // Isha
                "05:45", // Sunrise
                "17:50", // Sunset
                "04:20", // Imsak
                "00:00"  // LastThird
        );
    }
}

