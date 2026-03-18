package com.mosleemapp.app.data.remote.Responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CompleteQuranResponse {
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("surahs")
        public List<Surah> surahs;
    }

    public static class Surah {
        public int number;
        public String name;
        public String englishName;
        public String englishNameTranslation;
        public String revelationType;
        public List<Ayah> ayahs;
    }

    public static class Ayah {
        public int number;
        public String text;
        public int numberInSurah;
        public int juz;
        public int manzil;
        public int page;
        public int ruku;
        public int hizbQuarter;
        public Object sajda; // Handle boolean/object polymorphism
    }
}
