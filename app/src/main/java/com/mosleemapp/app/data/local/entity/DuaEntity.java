package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "duas")
public class DuaEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titleEn;
    public String titleId;
    public String arabic;
    public String latin;
    public String translationEn;
    public String translationId;
    public String category;
}
