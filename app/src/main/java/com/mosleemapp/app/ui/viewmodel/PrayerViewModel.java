package com.mosleemapp.app.ui.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.PrayerTimeEntity;
import com.mosleemapp.app.data.repository.PrayerRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrayerViewModel extends AndroidViewModel {
    private PrayerRepository repository;
    private final MutableLiveData<PrayerTimeEntity> prayerData = new MutableLiveData<>();
    private final MutableLiveData<String> nextPrayerName = new MutableLiveData<>();
    private final MutableLiveData<String> nextPrayerTimeRemaining = new MutableLiveData<>();
    
    // Default location (Jakarta)
    private double currentLat = -6.2088;
    private double currentLon = 106.8456;
    
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    public PrayerViewModel(@NonNull Application application) {
        super(application);
        repository = new PrayerRepository(application);
    }

    public void updateLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
        fetchPrayerTimes();
    }

    public void fetchPrayerTimes() {
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        repository.getPrayerTimes(currentLat, currentLon, 11, date).observeForever(entity -> {
            prayerData.postValue(entity);
            if (entity != null) {
                startCountdown(entity);
                com.mosleemapp.app.utils.AlarmScheduler.schedulePrayerAlarms(getApplication(), entity);
            }
        });
    }

    public LiveData<PrayerTimeEntity> getPrayerTimes() {
        return prayerData;
    }
    
    public LiveData<String> getNextPrayerName() { return nextPrayerName; }
    public LiveData<String> getNextPrayerTimeRemaining() { return nextPrayerTimeRemaining; }

    private void startCountdown(PrayerTimeEntity entity) {
        if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
        
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Mock calculation for MVP
                // In real app: parse entity.fajr, entity.dhuhr... find next.
                // For now, static countdown.
                nextPrayerName.postValue("Dzuhur");
                nextPrayerTimeRemaining.postValue("-00:45:00"); 
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
    }
}
