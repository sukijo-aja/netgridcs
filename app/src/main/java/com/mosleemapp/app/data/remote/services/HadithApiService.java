package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.remote.Responses.HadithBookResponse;
import com.mosleemapp.app.data.remote.Responses.HadithDetailResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HadithApiService {
    @GET("books")
    Call<HadithBookResponse> getBooks();

    @GET("books/{book}?range=1-300")
    Call<HadithDetailResponse> getHadithByBook(@retrofit2.http.Path("book") String book);
}
