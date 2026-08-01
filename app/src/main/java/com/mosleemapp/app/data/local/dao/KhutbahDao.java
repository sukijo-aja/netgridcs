package com.mosleemapp.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosleemapp.app.data.local.entity.KhutbahEntity;

import java.util.List;

@Dao
public interface KhutbahDao {
    @Query("SELECT * FROM khutbahs ORDER BY date DESC, id ASC")
    LiveData<List<KhutbahEntity>> getAllKhutbahs();

    @Query("SELECT * FROM khutbahs WHERE id = :id LIMIT 1")
    KhutbahEntity getKhutbahById(int id);

    @Query("SELECT * FROM khutbahs WHERE id = :id LIMIT 1")
    LiveData<KhutbahEntity> getKhutbahByIdLiveData(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<KhutbahEntity> khutbahs);

    @Query("DELETE FROM khutbahs")
    void deleteAll();
}
