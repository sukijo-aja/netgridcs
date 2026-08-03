package com.androidstarter.app.data.remote.services;

import com.androidstarter.app.data.model.DeviceTokenRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeviceApiService {
    // Simulasi menggunakan httpbin.org untuk mengembalikan payload request sebagai respons
    @POST("post")
    Call<ResponseBody> sendDeviceTokens(@Body DeviceTokenRequest request);
}
