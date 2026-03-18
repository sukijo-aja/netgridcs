package com.mosleemapp.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosleemapp.app.data.local.entity.HadithBookEntity;
import com.mosleemapp.app.data.local.entity.HadithEntity;

import java.util.List;

@Dao
public interface HadithDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBooks(List<HadithBookEntity> books);

    @Query("SELECT * FROM hadith_books")
    List<HadithBookEntity> getBooks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHadiths(List<HadithEntity> hadiths);

    @Query("SELECT * FROM hadiths WHERE bookId = :bookId ORDER BY number ASC")
    List<HadithEntity> getHadithsByBookId(String bookId);
    
    @Query("SELECT COUNT(*) FROM hadith_books")
    int getBookCount();
    
    @Query("SELECT COUNT(*) FROM hadiths")
    int getTotalHadithCount();
    
    @Query("SELECT COUNT(*) FROM hadiths WHERE bookId = :bookId")
    int getHadithCountByBookId(String bookId);
}
