package com.mosleemapp.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "custom_habit_logs",
        primaryKeys = {"date", "habitId"},
        foreignKeys = @ForeignKey(entity = CustomHabitEntity.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE))
public class CustomHabitLogEntity {
    @NonNull
    public String date; // Format: YYYY-MM-DD
    public int habitId;
    public boolean isCompleted;

    public CustomHabitLogEntity(@NonNull String date, int habitId, boolean isCompleted) {
        this.date = date;
        this.habitId = habitId;
        this.isCompleted = isCompleted;
    }
}
