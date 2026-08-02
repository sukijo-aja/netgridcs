package com.androidstarter.app.data.repository;

import android.content.Context;
import android.util.Log;

import com.androidstarter.app.data.model.VersionResponse;
import com.androidstarter.app.data.remote.services.VersionApiService;
import com.androidstarter.app.utils.AppPreference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VersionRepository {

    private static final String BASE_URL = "https://raw.githubusercontent.com/sukijo-aja/repodata/main/data/muslimapp/";
    private VersionApiService apiService;

    public VersionRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(VersionApiService.class);
    }

    public interface VersionCallback {
        void onComplete();
    }

    public void checkAndSyncVersion(Context context, VersionCallback callback) {
        apiService.getVersion().enqueue(new Callback<VersionResponse>() {
            @Override
            public void onResponse(Call<VersionResponse> call, Response<VersionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VersionResponse version = response.body();
                    AppPreference prefs = new AppPreference(context);
                    
                    if (version.getModules() != null) {
                        if (version.getModules().getHadist() != null) {
                            long serverHadistTime = version.getModules().getHadist().getLastUpdated();
                            long localHadistTime = prefs.getLong("hadist_last_updated", 0);
                            if (serverHadistTime > localHadistTime) {
                                prefs.saveBoolean("UPDATE_HADIST_REQUIRED", true);
                                prefs.saveLong("hadist_last_updated", serverHadistTime);
                            }
                        }
                        
                        if (version.getModules().getProducts() != null) {
                            prefs.saveLong("products_last_updated", version.getModules().getProducts().getLastUpdated());
                        }
                        
                        if (version.getModules().getKhutbah() != null) {
                            long serverKhutbahTime = version.getModules().getKhutbah().getLastUpdated();
                            long localKhutbahTime = prefs.getLong("khutbah_last_updated", 0);
                            if (serverKhutbahTime > localKhutbahTime) {
                                prefs.saveBoolean("UPDATE_KHUTBAH_REQUIRED", true);
                                prefs.saveLong("khutbah_last_updated", serverKhutbahTime);
                            }
                        }
                    }
                }
                
                if (callback != null) {
                    callback.onComplete();
                }
            }

            @Override
            public void onFailure(Call<VersionResponse> call, Throwable t) {
                Log.e("VersionRepository", "Failed to check version: " + t.getMessage());
                if (callback != null) {
                    callback.onComplete();
                }
            }
        });
    }
}
