package com.mosleemapp.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.dao.KhutbahDao;
import com.mosleemapp.app.data.local.entity.KhutbahEntity;
import com.mosleemapp.app.data.model.Khutbah;
import com.mosleemapp.app.data.model.KhutbahDetailResponse;
import com.mosleemapp.app.data.remote.services.KhutbahApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KhutbahRepository {
    private KhutbahDao khutbahDao;
    private KhutbahApiService apiService;
    private ExecutorService executor;

    public KhutbahRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        khutbahDao = db.khutbahDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/sukijo-aja/repodata/main/data/muslimapp/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(KhutbahApiService.class);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<KhutbahEntity>> getAllKhutbahs() {
        return khutbahDao.getAllKhutbahs();
    }

    public LiveData<KhutbahEntity> getKhutbahById(int id) {
        return khutbahDao.getKhutbahByIdLiveData(id);
    }

    public void syncKhutbahData(Runnable onComplete, Runnable onError) {
        executor.execute(() -> {
            try {
                Response<List<Khutbah>> listResponse = apiService.getKhutbahList().execute();
                if (listResponse.isSuccessful() && listResponse.body() != null) {
                    List<Khutbah> remoteList = listResponse.body();
                    List<KhutbahEntity> entityList = new ArrayList<>();

                    for (Khutbah khutbah : remoteList) {
                        KhutbahEntity entity = new KhutbahEntity();
                        entity.id = khutbah.getId();
                        entity.title = khutbah.getTitle();
                        entity.khotib = khutbah.getKhotib();
                        entity.date = khutbah.getDate();
                        entity.description = khutbah.getDescription();
                        entity.content_url = khutbah.getContentUrl();
                        entity.category_id = khutbah.getCategoryId();
                        entity.category = khutbah.getCategory();

                        // Fetch details
                        if (entity.content_url != null && !entity.content_url.isEmpty()) {
                            String url = "khutbah/" + entity.content_url;
                            Response<KhutbahDetailResponse> detailResponse = apiService.getKhutbahDetail(url).execute();
                            if (detailResponse.isSuccessful() && detailResponse.body() != null) {
                                entity.content = detailResponse.body().getContent();
                                entity.source = detailResponse.body().getSource();
                            }
                        }
                        entityList.add(entity);
                    }

                    khutbahDao.deleteAll();
                    khutbahDao.insertAll(entityList);

                    if (onComplete != null) onComplete.run();
                } else {
                    if (onError != null) onError.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) onError.run();
            }
        });
    }
}
