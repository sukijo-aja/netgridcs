package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "khutbahs")
public class KhutbahEntity {
    @PrimaryKey
    public int id;
    
    public String title;
    public String khotib;
    public String date;
    public String description;
    public String content_url;
    public int category_id;
    public String category;
    
    // Details (populated after fetching content_url)
    public String content;
    public String source;
}
