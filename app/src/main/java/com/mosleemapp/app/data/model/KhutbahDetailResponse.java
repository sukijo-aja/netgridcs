package com.mosleemapp.app.data.model;

import com.google.gson.annotations.SerializedName;

public class KhutbahDetailResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("khotib")
    private String khotib;

    @SerializedName("date")
    private String date;

    @SerializedName("category")
    private String category;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("description")
    private String description;

    @SerializedName("content")
    private String content;

    @SerializedName("source")
    private String source;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getKhotib() { return khotib; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public int getCategoryId() { return categoryId; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getSource() { return source; }
}
