package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "surahs")
public class SurahEntity {
    @PrimaryKey
    public int number;
    public String name;
    public String englishName;
    public String englishNameTranslation;
    public int numberOfAyahs;
    public String revelationType;
}
