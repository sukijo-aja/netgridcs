package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.model.Khutbah;
import com.mosleemapp.app.data.model.KhutbahDetailResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface KhutbahApiService {
    @GET("khutbah/list.json")
    Call<List<Khutbah>> getKhutbahList();

    @GET
    Call<KhutbahDetailResponse> getKhutbahDetail(@Url String url);
}
