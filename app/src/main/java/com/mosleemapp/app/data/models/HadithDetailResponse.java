package com.mosleemapp.app.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HadithDetailResponse {
    @SerializedName("data")
    public HadithData data;

    public static class HadithData {
        public String name;
        public String id;
        public int available;
        public int requested;
        public List<Hadith> hadiths;
    }

    public static class Hadith {
        public int number;
        public String arab;
        public String id;
    }
}
