package com.mosleemapp.app.data.remote;

import com.mosleemapp.app.data.models.AyahResponse;
import com.mosleemapp.app.data.models.SurahResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface QuranApiService {
    @GET("surah")
    Call<SurahResponse> getSurahs();

    @GET("surah/{number}/editions/menu_quran-uthmani,id.indonesian")
    Call<AyahResponse> getSurahDetail(@retrofit2.http.Path("number") int number);
}
