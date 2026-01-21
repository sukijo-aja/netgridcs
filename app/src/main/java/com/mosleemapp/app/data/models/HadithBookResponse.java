package com.mosleemapp.app.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HadithBookResponse {
    @SerializedName("data")
    public List<HadithBook> data;

    public static class HadithBook {
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
        @SerializedName("available")
        public int available;
    }
}
