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
import com.mosleemapp.app.utils.LocaleHelper;

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
    private Context context;
    private static final String BASE_URL = "https://api.alquran.cloud/v1/";

    public QuranRepository(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        quranDao = db.quranDao();
        executorService = Executors.newSingleThreadExecutor();
        
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .dns(com.mosleemapp.app.data.remote.DnsHelper.createGoogleDns())
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
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

    public void getSajdaAyahs(Callback<List<AyahResponse.Ayah>> callback) {
        executorService.execute(() -> {
            List<AyahEntity> localAyahs = quranDao.getAyahsWithSajda();
            if (localAyahs != null && !localAyahs.isEmpty()) {
                List<AyahResponse.Ayah> mappedAyahs = mapEntitiesToAyahs(localAyahs);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(mappedAyahs));
            } else {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("No Sajda Ayahs found locally. Please download complete Quran data first."));
            }
        });
    }

    private void fetchAyahsFromApi(int surahNumber, Callback<List<AyahResponse.Ayah>> callback) {
        String editions = "quran-uthmani,en.sahih,id.indonesian";

        apiService.getSurahDetail(surahNumber, editions).enqueue(new retrofit2.Callback<AyahResponse>() {
            @Override
            public void onResponse(Call<AyahResponse> call, Response<AyahResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null && !response.body().data.isEmpty()) {
                    List<AyahResponse.SurahDetail> data = response.body().data;
                    
                    // Assuming API returns in order of editions requested: 0=Arabic, 1=English, 2=Indonesian
                    // Ideally we should check edition field inside response but for now we rely on index if consistent
                    List<AyahResponse.Ayah> arabicAyahs = data.get(0).ayahs;
                    List<AyahResponse.Ayah> englishAyahs = (data.size() > 1) ? data.get(1).ayahs : null;
                    List<AyahResponse.Ayah> indoAyahs = (data.size() > 2) ? data.get(2).ayahs : null;
                    
                    
                    
                    executorService.execute(() -> {
                         // We map everything to entities first to save
                         quranDao.insertAyahs(mapAyahsToEntities(arabicAyahs, englishAyahs, indoAyahs, surahNumber));
                    });
                    
                    // For callback, we return a list where 'translation' field is populated based on current locale?
                    // OR we return a modified object.
                    // Actually, let's map back from the entities we just created (or mapped objects)
                    // But we are on background thread, so let's reuse api response for speed but we need to populate translation field for the UI to show *something* immediately?
                    // Better yet: return the objects with BOTH translations if possible, orlet adapter decide?
                    // The Adapter expects AyahResponse.Ayah which has 'translation' field.
                    // We should probably populate 'translation' with the current app language preference here for immediate display
                    
                    String currentLang = getLanguageCode();
                    for (int i = 0; i < arabicAyahs.size(); i++) {
                        if (currentLang.equals("id") && indoAyahs != null && i < indoAyahs.size()) {
                            arabicAyahs.get(i).translation = indoAyahs.get(i).text;
                        } else if (englishAyahs != null && i < englishAyahs.size()) {
                            arabicAyahs.get(i).translation = englishAyahs.get(i).text;
                        }
                    }

                    callback.onSuccess(arabicAyahs);
                } else {
                    callback.onError("Failed to fetch ayahs. Please check your connection.");
                }
            }

            @Override
            public void onFailure(Call<AyahResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private String getLanguageCode() {
         String lang = LocaleHelper.getLanguage(context);
         if (lang.equals("in")) return "id";
         if (lang.equals("ar")) return "ar";
         return "en";
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

    private List<AyahEntity> mapAyahsToEntities(List<AyahResponse.Ayah> arabicAyahs, List<AyahResponse.Ayah> enAyahs, List<AyahResponse.Ayah> idAyahs, int surahId) {
        List<AyahEntity> entities = new ArrayList<>();
        for (int i = 0; i < arabicAyahs.size(); i++) {
             AyahResponse.Ayah ayah = arabicAyahs.get(i);
            AyahEntity entity = new AyahEntity();
            entity.surahId = surahId;
            entity.number = ayah.number;
            entity.text = ayah.text;
            
            if (enAyahs != null && i < enAyahs.size()) {
                entity.translationEn = enAyahs.get(i).text;
            }
             if (idAyahs != null && i < idAyahs.size()) {
                entity.translationId = idAyahs.get(i).text;
            }
            
            entity.numberInSurah = ayah.numberInSurah;
            entity.juz = ayah.juz;
            entity.manzil = ayah.manzil;
            entity.page = ayah.page;
            entity.ruku = ayah.ruku;
            entity.hizbQuarter = ayah.hizbQuarter;
            if (ayah.sajda instanceof Boolean) {
                entity.sajda = (Boolean) ayah.sajda;
            } else {
                // If it's not a boolean (e.g. it's an object/LinkedTreeMap), it means there IS a sajda
                entity.sajda = ayah.sajda != null;
            }
            entities.add(entity);
        }
        return entities;
    }

    private List<AyahResponse.Ayah> mapEntitiesToAyahs(List<AyahEntity> entities) {
        String currentLang = getLanguageCode();
        List<AyahResponse.Ayah> ayahs = new ArrayList<>();
        for (AyahEntity entity : entities) {
            AyahResponse.Ayah ayah = new AyahResponse.Ayah();
            ayah.number = entity.number;
            ayah.text = entity.text;
            
            if (currentLang.equals("id")) {
                ayah.translation = entity.translationId;
            } else {
                ayah.translation = entity.translationEn;
            }
            
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

    public void downloadAllData(Callback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // 1. Fetch Arabic (quran-uthmani) - BASE
                Response<com.mosleemapp.app.data.models.CompleteQuranResponse> arResponse = apiService.getCompleteQuran("quran-uthmani").execute();
                if (!arResponse.isSuccessful() || arResponse.body() == null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Failed to fetch Arabic data"));
                    return;
                }

                // 2. Fetch English (en.sahih)
                Response<com.mosleemapp.app.data.models.CompleteQuranResponse> enResponse = apiService.getCompleteQuran("en.sahih").execute();
                
                // 3. Fetch Indonesian (id.indonesian)
                Response<com.mosleemapp.app.data.models.CompleteQuranResponse> idResponse = apiService.getCompleteQuran("id.indonesian").execute();

                List<com.mosleemapp.app.data.models.CompleteQuranResponse.Surah> arSurahs = arResponse.body().data.surahs;
                List<com.mosleemapp.app.data.models.CompleteQuranResponse.Surah> enSurahs = (enResponse.isSuccessful() && enResponse.body() != null) ? enResponse.body().data.surahs : null;
                List<com.mosleemapp.app.data.models.CompleteQuranResponse.Surah> idSurahs = (idResponse.isSuccessful() && idResponse.body() != null) ? idResponse.body().data.surahs : null;

                // Prepare Entities
                List<SurahEntity> surahEntities = new ArrayList<>();
                List<AyahEntity> ayahEntities = new ArrayList<>();

                for (int i = 0; i < arSurahs.size(); i++) {
                    com.mosleemapp.app.data.models.CompleteQuranResponse.Surah arSurah = arSurahs.get(i);
                    com.mosleemapp.app.data.models.CompleteQuranResponse.Surah enSurah = (enSurahs != null && i < enSurahs.size()) ? enSurahs.get(i) : null;
                    
                    // Map Surah
                    SurahEntity surahEntity = new SurahEntity();
                    surahEntity.number = arSurah.number;
                    surahEntity.name = arSurah.name;
                    surahEntity.englishName = arSurah.englishName;
                    surahEntity.englishNameTranslation = arSurah.englishNameTranslation;
                    surahEntity.numberOfAyahs = arSurah.ayahs.size();
                    surahEntity.revelationType = arSurah.revelationType;
                    surahEntities.add(surahEntity);

                    // Map Ayahs
                    for (int j = 0; j < arSurah.ayahs.size(); j++) {
                        com.mosleemapp.app.data.models.CompleteQuranResponse.Ayah arAyah = arSurah.ayahs.get(j);
                        AyahEntity ayahEntity = new AyahEntity();
                        ayahEntity.surahId = arSurah.number;
                        ayahEntity.number = arAyah.number;
                        ayahEntity.text = arAyah.text;
                        ayahEntity.numberInSurah = arAyah.numberInSurah;
                        ayahEntity.juz = arAyah.juz;
                        ayahEntity.manzil = arAyah.manzil;
                        ayahEntity.page = arAyah.page;
                        ayahEntity.ruku = arAyah.ruku;
                        ayahEntity.hizbQuarter = arAyah.hizbQuarter;
                        
                        // Handle Sajda
                         if (arAyah.sajda instanceof Boolean) {
                            ayahEntity.sajda = (Boolean) arAyah.sajda;
                        } else {
                            ayahEntity.sajda = arAyah.sajda != null;
                        }

                        // Translations
                        if (enSurah != null && j < enSurah.ayahs.size()) {
                            ayahEntity.translationEn = enSurah.ayahs.get(j).text;
                        }
                        
                        if (idSurahs != null && i < idSurahs.size()) {
                             com.mosleemapp.app.data.models.CompleteQuranResponse.Surah idSurah = idSurahs.get(i);
                             if (j < idSurah.ayahs.size()) {
                                 ayahEntity.translationId = idSurah.ayahs.get(j).text;
                             }
                        }

                        ayahEntities.add(ayahEntity);
                    }
                }

                // Bulk Insert
                quranDao.insertSurahs(surahEntities);
                quranDao.insertAyahs(ayahEntities);
                
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(true));

            } catch (Exception e) {
                e.printStackTrace();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Error downloading: " + e.getMessage()));
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
