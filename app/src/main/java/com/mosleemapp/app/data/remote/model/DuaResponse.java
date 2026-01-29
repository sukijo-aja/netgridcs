package com.mosleemapp.app.data.remote.model;

import com.google.gson.annotations.SerializedName;

public class DuaResponse {
    @SerializedName("id")
    public String id;

    @SerializedName("doa")
    public String title;

    @SerializedName("ayat")
    public String arabic;

    @SerializedName("latin")
    public String latin;

    @SerializedName("artinya")
    public String translation;
}
