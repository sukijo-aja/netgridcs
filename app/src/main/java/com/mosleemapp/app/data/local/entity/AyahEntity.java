package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ayahs",
        foreignKeys = @ForeignKey(entity = SurahEntity.class,
                parentColumns = "number",
                childColumns = "surahId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("surahId")})
public class AyahEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int surahId;
    public int number;
    public String text;
    public String translation;
    public int numberInSurah;
    public int juz;
    public int manzil;
    public int page;
    public int ruku;
    public int hizbQuarter;
    public boolean sajda;
}
