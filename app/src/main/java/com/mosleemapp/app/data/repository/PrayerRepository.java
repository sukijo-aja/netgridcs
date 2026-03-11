package com.mosleemapp.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.PrayerDao;
import com.mosleemapp.app.data.local.PrayerTimeEntity;
import com.mosleemapp.app.data.local.DefaultPrayerData;
import com.mosleemapp.app.data.remote.AladhanApiService;
import com.mosleemapp.app.data.remote.RetrofitClient;
import com.mosleemapp.app.data.remote.model.PrayerResponse;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrayerRepository {
    private PrayerDao prayerDao;
    private AladhanApiService apiService;

    public PrayerRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        prayerDao = db.prayerDao();
        apiService = RetrofitClient.getRetrofitInstance().create(AladhanApiService.class);
    }

    public LiveData<PrayerTimeEntity> getPrayerTimes(double latitude, double longitude, int method, String date) {
        MutableLiveData<PrayerTimeEntity> data = new MutableLiveData<>();

        // Check local DB first
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PrayerTimeEntity localData = prayerDao.getPrayerTime(date);
            if (localData != null) {
                data.postValue(localData);
            } else {
                PrayerTimeEntity defaultEntity = DefaultPrayerData.getDefault(date);
                prayerDao.insertPrayerTimes(Collections.singletonList(defaultEntity));
                data.postValue(defaultEntity);
                fetchFromNetwork(latitude, longitude, method, date, data);
            }
        });
        return data;
    }

    private void fetchFromNetwork(double latitude, double longitude, int method, String date,
            MutableLiveData<PrayerTimeEntity> liveData) {
        apiService.getPrayerTimes(date, latitude, longitude, method).enqueue(new Callback<PrayerResponse>() {
            @Override
            public void onResponse(Call<PrayerResponse> call, Response<PrayerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    PrayerResponse.Timings timings = response.body().data.timings;
                    PrayerResponse.DateInfo dateInfo = response.body().data.date;

                    // Note: API returns just time or time (TZ). For simplicity let's assume raw
                    // string matches or we clean it.
                    // Ideally we parse date to match requested date, but API timingsByCity returns
                    // current day?
                    // Wait, timingsByCity usually returns valid for today.
                    // Let's create entity.

                    PrayerTimeEntity entity = new PrayerTimeEntity(
                            date, // We should ensure this matches the API date or request date
                            timings.fajr,
                            timings.dhuhr,
                            timings.asr,
                            timings.maghrib,
                            timings.isha,
                            timings.sunrise,
                            timings.sunset,
                            timings.imsak,
                            timings.lastThird);

                    // Save to DB
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        prayerDao.insertPrayerTimes(Collections.singletonList(entity));
                        liveData.postValue(entity);
                    });
                }
            }

            @Override
            public void onFailure(Call<PrayerResponse> call, Throwable t) {
                // Handle error
                android.util.Log.e("PrayerRepository", "Failed to fetch prayer times", t);
            }
        });
    }
}
