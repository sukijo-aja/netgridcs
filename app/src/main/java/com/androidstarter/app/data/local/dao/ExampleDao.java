package com.androidstarter.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.androidstarter.app.data.local.entity.ExampleEntity;
import java.util.List;

@Dao
public interface ExampleDao {
    @Insert
    void insert(ExampleEntity entity);

    @Query("SELECT * FROM example_table")
    List<ExampleEntity> getAll();
}
