package com.mosleemapp.app.data.remote;

import com.mosleemapp.app.data.models.HadithBookResponse;
import com.mosleemapp.app.data.models.HadithDetailResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HadithApiService {
    @GET("books")
    Call<HadithBookResponse> getBooks();

    @GET("books/{book}?range=1-300")
    Call<HadithDetailResponse> getHadithByBook(@retrofit2.http.Path("book") String book);
}
