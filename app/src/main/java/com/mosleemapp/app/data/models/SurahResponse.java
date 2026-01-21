package com.mosleemapp.app.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SurahResponse {
    @SerializedName("data")
    public List<Surah> data;

    public static class Surah {
        public int number;
        public String name;
        @SerializedName("englishName")
        public String englishName;
        @SerializedName("englishNameTranslation")
        public String englishNameTranslation;
        public int numberOfAyahs;
        public String revelationType;
    }
}
