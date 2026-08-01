package com.mosleemapp.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Khutbah {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("khotib")
    private String khotib;

    @SerializedName("date")
    private String date;

    @SerializedName("description")
    private String description;

    @SerializedName("content_url")
    private String contentUrl;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category")
    private String category;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getKhotib() { return khotib; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getContentUrl() { return contentUrl; }
    public int getCategoryId() { return categoryId; }
    public String getCategory() { return category; }
}
