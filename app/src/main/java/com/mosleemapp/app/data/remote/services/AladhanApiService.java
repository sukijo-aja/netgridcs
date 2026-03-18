package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.remote.Responses.PrayerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AladhanApiService {
    @GET("timings/{date}")
    Call<PrayerResponse> getPrayerTimes(
            @Path("date") String date,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("method") int method);
}
