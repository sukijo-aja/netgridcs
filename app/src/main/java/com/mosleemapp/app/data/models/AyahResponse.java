package com.mosleemapp.app.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AyahResponse {
    @SerializedName("data")
    public List<SurahDetail> data;

    public static class SurahDetail {
        public int number;
        public String name;
        @SerializedName("englishName")
        public String englishName;
        @SerializedName("englishNameTranslation")
        public String englishNameTranslation;
        public int numberOfAyahs;
        public String revelationType;
        @SerializedName("ayahs")
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
        public boolean sajda;
        public String translation;
    }
}
