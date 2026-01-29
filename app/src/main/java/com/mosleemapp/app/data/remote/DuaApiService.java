package com.mosleemapp.app.data.remote;

import com.mosleemapp.app.data.remote.model.DuaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DuaApiService {
    @GET("api")
    Call<List<DuaResponse>> getDuas();
}
