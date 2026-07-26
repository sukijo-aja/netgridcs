package com.mosleemapp.app.data.remote.services;

import com.mosleemapp.app.data.model.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductApiService {
    @GET("products/list.json")
    Call<List<Product>> getProducts();
}
