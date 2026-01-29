package com.mosleemapp.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mosleemapp.app.data.local.entity.DuaEntity;

import java.util.List;

@Dao
public interface DuaDao {

    @Query("SELECT * FROM duas")
    LiveData<List<DuaEntity>> getAllDuas();

    @Query("SELECT * FROM duas WHERE category = :category")
    LiveData<List<DuaEntity>> getDuasByCategory(String category);

    @Query("SELECT DISTINCT category FROM duas")
    LiveData<List<String>> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DuaEntity> duas);

    @Query("SELECT COUNT(id) FROM duas")
    int getDuaCount();
}
