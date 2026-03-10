package com.mosleemapp.app.ui.viewmodel;

import android.annotation.SuppressLint;
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
import com.mosleemapp.app.utils.AppPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrayerViewModel extends AndroidViewModel {
    private PrayerRepository repository;
    private final MutableLiveData<PrayerTimeEntity> prayerData = new MutableLiveData<>();
    private final MutableLiveData<String> nextPrayerName = new MutableLiveData<>();
    private final MutableLiveData<String> nextPrayerTimeRemaining = new MutableLiveData<>();
    private final MutableLiveData<String> cityName = new MutableLiveData<>();
    private AppPreference appPreference;
    
    // Default location (Jakarta)
    private double currentLat = -6.2088;
    private double currentLon = 106.8456;
    
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    public PrayerViewModel(@NonNull Application application) {
        super(application);
        repository = new PrayerRepository(application);
        appPreference = new AppPreference(application.getApplicationContext());
    }

    public void updateLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
        appPreference.saveDouble("lat", lat);
        appPreference.saveDouble("lon", lon);
        fetchPrayerTimes();
        resolveCity(lat, lon);
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
    public LiveData<String> getCityName() { return cityName; }

    private void resolveCity(double lat, double lon) {
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(
                        getApplication(), Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    StringBuilder result = new StringBuilder();
                    android.location.Address address = addresses.get(0);
                    String city = address.getLocality();

                    result.append(city);
                    appPreference.saveString("city", result.toString());
                    cityName.postValue(result.toString());
                }
            } catch (Exception e) {
                Log.e("PrayerViewModel", "Geocoder failed", e);
            }
        }).start();
    }

    private void startCountdown(PrayerTimeEntity entity) {
        if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
        
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Calendar now = Calendar.getInstance();
                    
                    String[] prayerNames = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
                    String[] prayerTimes = {entity.fajr, entity.dhuhr, entity.asr, entity.maghrib, entity.isha};
                    
                    String nextName = "Fajr (Tomorrow)";
                    long minDiff = Long.MAX_VALUE;
                    
                    for (int i = 0; i < prayerTimes.length; i++) {
                        if (prayerTimes[i] == null) continue;
                        String cleanTime = prayerTimes[i].split(" ")[0];
                        Date date = sdf.parse(cleanTime);
                        if (date != null) {
                            Calendar pTime = Calendar.getInstance();
                            pTime.set(Calendar.HOUR_OF_DAY, date.getHours());
                            pTime.set(Calendar.MINUTE, date.getMinutes());
                            pTime.set(Calendar.SECOND, 0);
                            
                            long diff = pTime.getTimeInMillis() - now.getTimeInMillis();
                            if (diff > 0 && diff < minDiff) {
                                minDiff = diff;
                                nextName = prayerNames[i];
                            }
                        }
                    }
                    
                    if (minDiff == Long.MAX_VALUE) {
                        // All prayers today have passed, calculate Fajr for tomorrow
                        if (prayerTimes[0] != null) {
                            String cleanTime = prayerTimes[0].split(" ")[0];
                            Date date = sdf.parse(cleanTime);
                            if (date != null) {
                                Calendar pTime = Calendar.getInstance();
                                pTime.add(Calendar.DAY_OF_YEAR, 1);
                                pTime.set(Calendar.HOUR_OF_DAY, date.getHours());
                                pTime.set(Calendar.MINUTE, date.getMinutes());
                                pTime.set(Calendar.SECOND, 0);
                                minDiff = pTime.getTimeInMillis() - now.getTimeInMillis();
                                nextName = "Fajr";
                            }
                        }
                    }
                    
                    if (minDiff != Long.MAX_VALUE) {
                        long secondsInMilli = 1000;
                        long minutesInMilli = secondsInMilli * 60;
                        long hoursInMilli = minutesInMilli * 60;

                        long elapsedHours = minDiff / hoursInMilli;
                        minDiff = minDiff % hoursInMilli;

                        long elapsedMinutes = minDiff / minutesInMilli;
                        minDiff = minDiff % minutesInMilli;

                        long elapsedSeconds = minDiff / secondsInMilli;
                        
                        @SuppressLint("DefaultLocale") String timeRemaining = String.format("-%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
                        
                        nextPrayerName.postValue(nextName);
                        nextPrayerTimeRemaining.postValue(timeRemaining);
                    }
                } catch (Exception e) {
                    Log.e("PrayerViewModel", "Error calculating next prayer", e);
                }
                
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
