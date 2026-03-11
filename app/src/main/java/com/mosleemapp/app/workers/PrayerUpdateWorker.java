package com.mosleemapp.app.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.PrayerTimeEntity;
import com.mosleemapp.app.data.remote.AladhanApiService;
import com.mosleemapp.app.data.remote.RetrofitClient;
import com.mosleemapp.app.data.remote.model.PrayerResponse;
import com.mosleemapp.app.utils.AlarmScheduler;
import com.mosleemapp.app.utils.AppPreference;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import retrofit2.Response;

public class PrayerUpdateWorker extends Worker {

    private static final String TAG = "PrayerUpdateWorker";

    public PrayerUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting periodic prayer time update");
        
        AppPreference prefs = new AppPreference(getApplicationContext());
        double lat = prefs.getDouble("lat", 0.0);
        double lon = prefs.getDouble("lon", 0.0);

        if (lat == 0.0 && lon == 0.0) {
            Log.w(TAG, "Coordinates not set. Cannot update prayer times.");
            return Result.failure();
        }

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        try {
            AladhanApiService apiService = RetrofitClient.getRetrofitInstance().create(AladhanApiService.class);
            // Defaulting method to 11 (MUIS), matching standard repo usage
            Response<PrayerResponse> response = apiService.getPrayerTimes(date, lat, lon, 11).execute();

            if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                PrayerResponse.Timings timings = response.body().data.timings;

                PrayerTimeEntity entity = new PrayerTimeEntity(
                        date,
                        timings.fajr,
                        timings.dhuhr,
                        timings.asr,
                        timings.maghrib,
                        timings.isha,
                        timings.sunrise,
                        timings.sunset,
                        timings.imsak,
                        timings.lastThird);

                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                db.prayerDao().insertPrayerTimes(Collections.singletonList(entity));
                
                // Reschedule alarms using newly fetched entity
                AlarmScheduler.schedulePrayerAlarms(getApplicationContext(), entity);
                
                Log.d(TAG, "Successfully updated prayer times for " + date);
                return Result.success();
            } else {
                Log.e(TAG, "API call failed or empty response");
                return Result.retry(); // Retreat/Retry backoff on failure
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching prayer times in worker", e);
            return Result.retry();
        }
    }
}
