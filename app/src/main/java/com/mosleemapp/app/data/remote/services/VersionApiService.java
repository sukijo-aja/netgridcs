package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.model.VersionResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface VersionApiService {
    @GET("version.json")
    Call<VersionResponse> getVersion();
}
