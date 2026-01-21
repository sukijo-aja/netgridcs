package com.mosleemapp.app.data.remote;

import com.mosleemapp.app.data.remote.model.PrayerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AladhanApiService {
    @GET("timings")
    Call<PrayerResponse> getPrayerTimes(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("method") int method);
}
