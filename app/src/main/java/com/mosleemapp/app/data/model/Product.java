package com.mosleemapp.app.data.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("price")
    private int price;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("affiliate_link")
    private String url;
    
    @SerializedName("description")
    private String description;

    public Product(int id, String name, int price, String imageUrl, String url, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.url = url;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getUrl() { return url; }
    public String getDescription() { return description; }
}
