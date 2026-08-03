package com.androidstarter.app.data.remote;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reusable Retrofit callback that automatically wraps responses into Resource<T>.
 * Usage:
 *   apiService.getData().enqueue(new ApiCallback<MyData>() {
 *       @Override
 *       public void onResult(Resource<MyData> resource) {
 *           if (resource.isSuccess()) { ... }
 *           else if (resource.isError()) { ... }
 *       }
 *   });
 */
public abstract class ApiCallback<T> implements Callback<T> {

    public abstract void onResult(Resource<T> resource);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful() && response.body() != null) {
            onResult(Resource.success(response.body()));
        } else {
            String errorMsg = "Error " + response.code();
            try {
                if (response.errorBody() != null) {
                    errorMsg = response.errorBody().string();
                }
            } catch (Exception ignored) {}
            onResult(Resource.error(errorMsg));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String message = t.getMessage() != null ? t.getMessage() : "Unknown network error";
        onResult(Resource.error(message));
    }
}
