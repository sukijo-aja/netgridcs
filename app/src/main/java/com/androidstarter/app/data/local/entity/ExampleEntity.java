package com.androidstarter.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "example_table")
public class ExampleEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}
