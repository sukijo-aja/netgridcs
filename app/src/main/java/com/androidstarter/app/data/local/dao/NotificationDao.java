package com.androidstarter.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.androidstarter.app.data.local.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getAllNotifications();

    @Query("DELETE FROM notifications")
    void deleteAll();
    
    @Query("UPDATE notifications SET isRead = 1")
    void markAllAsRead();
    
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(int id);
}
