package com.androidstarter.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String message;
    public String messageId;
    public String actionType;
    public long timestamp;
    public boolean isRead;

    public NotificationEntity(String title, String message, String messageId, String actionType, long timestamp, boolean isRead) {
        this.title = title;
        this.message = message;
        this.messageId = messageId;
        this.actionType = actionType;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
}
