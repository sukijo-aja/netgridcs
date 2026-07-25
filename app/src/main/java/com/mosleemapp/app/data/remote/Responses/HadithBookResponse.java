package com.mosleemapp.app.data.remote.Responses;

import com.google.gson.annotations.SerializedName;

public class HadithBookResponse {

    public static class HadithBook {
        @SerializedName("slug")
        public String id;
        @SerializedName("name")
        public String name;
        @SerializedName("total")
        public int available;
    }
}
