package com.mosleemapp.app.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.entity.CustomHabitEntity;
import com.mosleemapp.app.data.local.entity.CustomHabitLogEntity;
import com.mosleemapp.app.data.local.entity.PrayerTrackerEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrayerTrackerViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final Calendar currentCalendar;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final MutableLiveData<Date> selectedDate = new MutableLiveData<>();
    private final LiveData<PrayerTrackerEntity> currentTrackerEntity;
    private final LiveData<List<CustomHabitLogEntity>> currentCustomHabitLogs;
    private final LiveData<List<CustomHabitEntity>> allCustomHabits;

    public PrayerTrackerViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        currentCalendar = Calendar.getInstance();
        selectedDate.setValue(currentCalendar.getTime());

        currentTrackerEntity = Transformations.switchMap(selectedDate, date -> {
            String dateStr = dateFormat.format(date);
            return db.prayerTrackerDao().getTrackerForDate(dateStr);
        });

        currentCustomHabitLogs = Transformations.switchMap(selectedDate, date -> {
            String dateStr = dateFormat.format(date);
            return db.customHabitDao().getLogsForDate(dateStr);
        });

        allCustomHabits = db.customHabitDao().getAllHabits();
    }

    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<PrayerTrackerEntity> getCurrentTrackerEntity() {
        return currentTrackerEntity;
    }

    public LiveData<List<CustomHabitLogEntity>> getCurrentCustomHabitLogs() {
        return currentCustomHabitLogs;
    }

    public LiveData<List<CustomHabitEntity>> getAllCustomHabits() {
        return allCustomHabits;
    }

    public void changeDate(int daysOffset) {
        currentCalendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        selectedDate.setValue(currentCalendar.getTime());
    }

    public void updatePrayer(String dateString, PrayerTrackerEntity currentEntity, boolean fajr, boolean dhuhr, boolean asr, boolean maghrib, boolean isha, boolean tilawah, boolean tahajud, boolean duha, boolean fast) {
        new Thread(() -> {
            PrayerTrackerEntity entityToUpdate = currentEntity;
            if (entityToUpdate == null) {
                entityToUpdate = new PrayerTrackerEntity(dateString);
            }
            entityToUpdate.fajr = fajr;
            entityToUpdate.dhuhr = dhuhr;
            entityToUpdate.asr = asr;
            entityToUpdate.maghrib = maghrib;
            entityToUpdate.isha = isha;
            
            entityToUpdate.tilawah = tilawah;
            entityToUpdate.tahajud = tahajud;
            entityToUpdate.duha = duha;
            entityToUpdate.fast = fast;
            
            db.prayerTrackerDao().insertOrUpdate(entityToUpdate);
        }).start();
    }

    public void addCustomHabit(String name) {
        new Thread(() -> {
            db.customHabitDao().insertHabit(new CustomHabitEntity(name, System.currentTimeMillis()));
        }).start();
    }

    public void updateCustomHabitLog(String dateString, int habitId, boolean isCompleted) {
        new Thread(() -> {
            CustomHabitLogEntity log = new CustomHabitLogEntity(dateString, habitId, isCompleted);
            db.customHabitDao().insertOrUpdateLog(log);
        }).start();
    }

    public void deleteCustomHabit(CustomHabitEntity habit) {
        new Thread(() -> {
            db.customHabitDao().deleteHabit(habit);
        }).start();
    }
}
