package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.remote.Responses.HadithBookResponse;
import com.mosleemapp.app.data.remote.Responses.HadithDetailResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HadithApiService {
    @GET("src/data/list.json")
    Call<java.util.List<HadithBookResponse.HadithBook>> getBooks();

    @GET("src/data/{book}.json")
    Call<java.util.List<HadithDetailResponse.Hadith>> getHadithByBook(@retrofit2.http.Path("book") String book);
}
