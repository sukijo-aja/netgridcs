package com.mosleemapp.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosleemapp.app.data.local.entity.AyahEntity;
import com.mosleemapp.app.data.local.entity.SurahEntity;

import java.util.List;

@Dao
public interface QuranDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSurahs(List<SurahEntity> surahs);

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    List<SurahEntity> getSurahs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAyahs(List<AyahEntity> ayahs);

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY numberInSurah ASC")
    List<AyahEntity> getAyahsBySurahId(int surahId);
    
    @Query("SELECT COUNT(*) FROM surahs")
    int getSurahCount();
    
    @Query("SELECT COUNT(*) FROM ayahs WHERE surahId = :surahId")
    int getAyahCountBySurahId(int surahId);

    @Query("DELETE FROM surahs")
    void deleteAllSurahs();

    @Query("DELETE FROM ayahs")
    void deleteAllAyahs();
}
