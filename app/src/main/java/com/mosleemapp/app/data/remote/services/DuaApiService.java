package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.remote.Responses.DuaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DuaApiService {
    @GET("api")
    Call<List<DuaResponse>> getDuas();
}
