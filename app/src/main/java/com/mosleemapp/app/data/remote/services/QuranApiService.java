package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.remote.Responses.AyahResponse;
import com.mosleemapp.app.data.remote.Responses.SurahResponse;
import com.mosleemapp.app.data.remote.Responses.CompleteQuranResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuranApiService {
    @GET("surah")
    Call<SurahResponse> getSurahs();

    @GET("surah/{number}/editions/{editions}")
    Call<AyahResponse> getSurahDetail(@retrofit2.http.Path("number") int number, @retrofit2.http.Path("editions") String editions);

    @GET("quran/{edition}")
    Call<CompleteQuranResponse> getCompleteQuran(@retrofit2.http.Path("edition") String edition);
}
