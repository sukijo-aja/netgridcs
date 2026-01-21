package com.mosleemapp.app.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class PrayerResponse {
    @SerializedName("code")
    public int code;

    @SerializedName("status")
    public String status;

    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("timings")
        public Timings timings;

        @SerializedName("date")
        public DateInfo date;
    }

    public static class Timings {
        @SerializedName("Fajr")
        public String fajr;
        @SerializedName("Dhuhr")
        public String dhuhr;
        @SerializedName("Asr")
        public String asr;
        @SerializedName("Maghrib")
        public String maghrib;
        @SerializedName("Isha")
        public String isha;
    }

    public static class DateInfo {
        @SerializedName("readable")
        public String readable;
    }
}
