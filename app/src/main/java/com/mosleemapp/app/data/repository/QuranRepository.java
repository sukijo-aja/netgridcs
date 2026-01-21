package com.mosleemapp.app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.dao.QuranDao;
import com.mosleemapp.app.data.local.entity.AyahEntity;
import com.mosleemapp.app.data.local.entity.SurahEntity;
import com.mosleemapp.app.data.models.AyahResponse;
import com.mosleemapp.app.data.models.SurahResponse;
import com.mosleemapp.app.data.remote.QuranApiService;
import com.mosleemapp.app.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuranRepository {

    private QuranDao quranDao;
    private QuranApiService apiService;
    private ExecutorService executorService;
    private static final String BASE_URL = "https://api.alquran.cloud/v1/";

    public QuranRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        quranDao = db.quranDao();
        executorService = Executors.newSingleThreadExecutor();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(QuranApiService.class);
    }

    public void getSurahs(Callback<List<SurahResponse.Surah>> callback) {
        executorService.execute(() -> {
            List<SurahEntity> localSurahs = quranDao.getSurahs();
            if (localSurahs != null && !localSurahs.isEmpty()) {
                List<SurahResponse.Surah> mappedSurahs = mapEntitiesToSurahs(localSurahs);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(mappedSurahs));
            } else {
                fetchSurahsFromApi(callback);
            }
        });
    }

    private void fetchSurahsFromApi(Callback<List<SurahResponse.Surah>> callback) {
        apiService.getSurahs().enqueue(new retrofit2.Callback<SurahResponse>() {
            @Override
            public void onResponse(Call<SurahResponse> call, Response<SurahResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SurahResponse.Surah> surahs = response.body().data;
                    executorService.execute(() -> {
                        quranDao.insertSurahs(mapSurahsToEntities(surahs));
                    });
                    callback.onSuccess(surahs);
                } else {
                    callback.onError("Failed to fetch data");
                }
            }

            @Override
            public void onFailure(Call<SurahResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAyahs(int surahNumber, Callback<List<AyahResponse.Ayah>> callback) {
        executorService.execute(() -> {
            List<AyahEntity> localAyahs = quranDao.getAyahsBySurahId(surahNumber);
            if (localAyahs != null && !localAyahs.isEmpty()) {
                List<AyahResponse.Ayah> mappedAyahs = mapEntitiesToAyahs(localAyahs);
                 new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(mappedAyahs));
            } else {
                fetchAyahsFromApi(surahNumber, callback);
            }
        });
    }

    private void fetchAyahsFromApi(int surahNumber, Callback<List<AyahResponse.Ayah>> callback) {
        apiService.getSurahDetail(surahNumber).enqueue(new retrofit2.Callback<AyahResponse>() {
            @Override
            public void onResponse(Call<AyahResponse> call, Response<AyahResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {
                    List<AyahResponse.SurahDetail> data = response.body().data;
                    List<AyahResponse.Ayah> arabicAyahs = data.get(0).ayahs;
                    
                    if (data.size() > 1) {
                         List<AyahResponse.Ayah> translationAyahs = data.get(1).ayahs;
                         for (int i = 0; i < arabicAyahs.size(); i++) {
                             if (i < translationAyahs.size()) {
                                 arabicAyahs.get(i).translation = translationAyahs.get(i).text;
                             }
                         }
                    }

                    executorService.execute(() -> {
                        quranDao.insertAyahs(mapAyahsToEntities(arabicAyahs, surahNumber));
                    });
                    callback.onSuccess(arabicAyahs);
                } else {
                    callback.onError("Failed to fetch ayahs");
                }
            }

            @Override
            public void onFailure(Call<AyahResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    
    // Mappers
    private List<SurahEntity> mapSurahsToEntities(List<SurahResponse.Surah> surahs) {
        List<SurahEntity> entities = new ArrayList<>();
        for (SurahResponse.Surah surah : surahs) {
            SurahEntity entity = new SurahEntity();
            entity.number = surah.number;
            entity.name = surah.name;
            entity.englishName = surah.englishName;
            entity.englishNameTranslation = surah.englishNameTranslation;
            entity.numberOfAyahs = surah.numberOfAyahs;
            entity.revelationType = surah.revelationType;
            entities.add(entity);
        }
        return entities;
    }

    private List<SurahResponse.Surah> mapEntitiesToSurahs(List<SurahEntity> entities) {
        List<SurahResponse.Surah> surahs = new ArrayList<>();
        for (SurahEntity entity : entities) {
            SurahResponse.Surah surah = new SurahResponse.Surah();
            surah.number = entity.number;
            surah.name = entity.name;
            surah.englishName = entity.englishName;
            surah.englishNameTranslation = entity.englishNameTranslation;
            surah.numberOfAyahs = entity.numberOfAyahs;
            surah.revelationType = entity.revelationType;
            surahs.add(surah);
        }
        return surahs;
    }

    private List<AyahEntity> mapAyahsToEntities(List<AyahResponse.Ayah> ayahs, int surahId) {
        List<AyahEntity> entities = new ArrayList<>();
        for (AyahResponse.Ayah ayah : ayahs) {
            AyahEntity entity = new AyahEntity();
            entity.surahId = surahId;
            entity.number = ayah.number;
            entity.text = ayah.text;
            entity.translation = ayah.translation;
            entity.numberInSurah = ayah.numberInSurah;
            entity.juz = ayah.juz;
            entity.manzil = ayah.manzil;
            entity.page = ayah.page;
            entity.ruku = ayah.ruku;
            entity.hizbQuarter = ayah.hizbQuarter;
            entity.sajda = ayah.sajda;
            entities.add(entity);
        }
        return entities;
    }

    private List<AyahResponse.Ayah> mapEntitiesToAyahs(List<AyahEntity> entities) {
        List<AyahResponse.Ayah> ayahs = new ArrayList<>();
        for (AyahEntity entity : entities) {
            AyahResponse.Ayah ayah = new AyahResponse.Ayah();
            ayah.number = entity.number;
            ayah.text = entity.text;
            ayah.translation = entity.translation;
            ayah.numberInSurah = entity.numberInSurah;
            ayah.juz = entity.juz;
            ayah.manzil = entity.manzil;
            ayah.page = entity.page;
            ayah.ruku = entity.ruku;
            ayah.hizbQuarter = entity.hizbQuarter;
            ayah.sajda = entity.sajda;
            ayahs.add(ayah);
        }
        return ayahs;
    }

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
