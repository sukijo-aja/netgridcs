package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_habits")
public class CustomHabitEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public long createdDate;

    public CustomHabitEntity(String name, long createdDate) {
        this.name = name;
        this.createdDate = createdDate;
    }
}
