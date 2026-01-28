package com.mosleemapp.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "hadiths",
        foreignKeys = @ForeignKey(entity = HadithBookEntity.class,
                parentColumns = "id",
                childColumns = "bookId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("bookId")})
public class HadithEntity {
    @PrimaryKey(autoGenerate = true)
    public int dbId;
    @NonNull
    public String bookId;
    public int number;
    public String arab;
    public String translation;
    public String language;
}
