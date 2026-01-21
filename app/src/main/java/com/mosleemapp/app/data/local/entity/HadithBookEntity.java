package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "hadith_books")
public class HadithBookEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public int available;
}
