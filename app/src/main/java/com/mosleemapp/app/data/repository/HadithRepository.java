package com.mosleemapp.app.data.repository;

import android.content.Context;
import android.util.Log;

import com.mosleemapp.app.data.local.AppDatabase;
import com.mosleemapp.app.data.local.dao.HadithDao;
import com.mosleemapp.app.data.local.entity.HadithBookEntity;
import com.mosleemapp.app.data.local.entity.HadithEntity;
import com.mosleemapp.app.data.remote.Responses.HadithBookResponse;
import com.mosleemapp.app.data.remote.Responses.HadithDetailResponse;
import com.mosleemapp.app.data.remote.services.HadithApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HadithRepository {

    private HadithDao hadithDao;
    private HadithApiService apiService;
    private ExecutorService executorService;
    private Context context;
    private static final String BASE_URL = "https://raw.githubusercontent.com/sukijo-aja/repodata/main/data/muslimapp/";

    public HadithRepository(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        hadithDao = db.hadithDao();
        executorService = Executors.newSingleThreadExecutor();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(HadithApiService.class);
    }

    public void getBooks(QuranRepository.Callback<List<HadithBookResponse.HadithBook>> callback) {
        executorService.execute(() -> {
            List<HadithBookEntity> localBooks = hadithDao.getBooks();
            if (localBooks != null && !localBooks.isEmpty()) {
                List<HadithBookResponse.HadithBook> mappedBooks = mapEntitiesToBooks(localBooks);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(mappedBooks));
            } else {
                fetchBooksFromApi(callback);
            }
        });
    }

    private void fetchBooksFromApi(QuranRepository.Callback<List<HadithBookResponse.HadithBook>> callback) {
        apiService.getBooks().enqueue(new Callback<List<HadithBookResponse.HadithBook>>() {
            @Override
            public void onResponse(Call<List<HadithBookResponse.HadithBook>> call, Response<List<HadithBookResponse.HadithBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HadithBookResponse.HadithBook> books = response.body();
                    executorService.execute(() -> {
                        hadithDao.insertBooks(mapBooksToEntities(books));
                    });
                    callback.onSuccess(books);
                } else {
                    callback.onError("Failed to fetch hadith books");
                }
            }

            @Override
            public void onFailure(Call<List<HadithBookResponse.HadithBook>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getHadiths(String bookId, QuranRepository.Callback<List<HadithDetailResponse.Hadith>> callback) {
        executorService.execute(() -> {
            List<HadithEntity> localHadiths = hadithDao.getHadithsByBookId(bookId);
            if (localHadiths != null && !localHadiths.isEmpty()) {
                List<HadithDetailResponse.Hadith> mappedHadiths = mapEntitiesToHadiths(localHadiths);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(mappedHadiths));
            } else {
                fetchHadithsFromApi(bookId, callback);
            }
        });
    }

    private void fetchHadithsFromApi(String bookId, QuranRepository.Callback<List<HadithDetailResponse.Hadith>> callback) {
        apiService.getHadithByBook(bookId).enqueue(new Callback<List<HadithDetailResponse.Hadith>>() {
            @Override
            public void onResponse(Call<List<HadithDetailResponse.Hadith>> call, Response<List<HadithDetailResponse.Hadith>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HadithDetailResponse.Hadith> hadiths = response.body();
                    executorService.execute(() -> {
                        hadithDao.insertHadiths(mapHadithsToEntities(hadiths, bookId));
                    });
                     callback.onSuccess(hadiths);
                } else {
                     callback.onError("Failed to fetch hadiths");
                }
            }

            @Override
            public void onFailure(Call<List<HadithDetailResponse.Hadith>> call, Throwable t) {
                 callback.onError(t.getMessage());
            }
        });
    }

    // Mappers
    private List<HadithBookEntity> mapBooksToEntities(List<HadithBookResponse.HadithBook> books) {
        List<HadithBookEntity> entities = new ArrayList<>();
        for (HadithBookResponse.HadithBook book : books) {
            HadithBookEntity entity = new HadithBookEntity();
            entity.id = book.id;
            entity.name = book.name;
            entity.available = book.available;
            entities.add(entity);
        }
        return entities;
    }

    private List<HadithBookResponse.HadithBook> mapEntitiesToBooks(List<HadithBookEntity> entities) {
        List<HadithBookResponse.HadithBook> books = new ArrayList<>();
        for (HadithBookEntity entity : entities) {
            HadithBookResponse.HadithBook book = new HadithBookResponse.HadithBook();
            book.id = entity.id;
            book.name = entity.name;
            book.available = entity.available;
            books.add(book);
        }
        return books;
    }

    private List<HadithEntity> mapHadithsToEntities(List<HadithDetailResponse.Hadith> hadiths, String bookId) {
        List<HadithEntity> entities = new ArrayList<>();
        for (HadithDetailResponse.Hadith hadith : hadiths) {
            HadithEntity entity = new HadithEntity();
            entity.bookId = bookId;
            entity.number = hadith.number;
            entity.arab = hadith.arab;
            entity.translation = hadith.id;
            entities.add(entity);
        }
        return entities;
    }

    private List<HadithDetailResponse.Hadith> mapEntitiesToHadiths(List<HadithEntity> entities) {
        List<HadithDetailResponse.Hadith> hadiths = new ArrayList<>();
        for (HadithEntity entity : entities) {
            HadithDetailResponse.Hadith hadith = new HadithDetailResponse.Hadith();
            hadith.number = entity.number;
            hadith.arab = entity.arab;
            hadith.id = entity.translation;
            hadiths.add(hadith);
        }
        return hadiths;
    }

    public void downloadAllData(QuranRepository.Callback<Boolean> callback) {
        executorService.execute(() -> {
            com.mosleemapp.app.utils.AppPreference prefs = new com.mosleemapp.app.utils.AppPreference(context);
            if (prefs.getBoolean("UPDATE_HADIST_REQUIRED", false)) {
                Log.d("HadithRepository", "Update required, clearing hadith cache.");
                hadithDao.clearHadiths();
                hadithDao.clearBooks();
                prefs.saveBoolean("UPDATE_HADIST_REQUIRED", false);
            }

            // Guard: skip if Hadith is already complete (books and hadiths exist)
            int bookCount = hadithDao.getBookCount();
            int hadithCount = hadithDao.getTotalHadithCount();
            if (bookCount > 0 && hadithCount > 0) {
                Log.d("HadithRepository", "Hadith already complete (books=" + bookCount + ", hadiths=" + hadithCount + "). Skipping download.");
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(true));
                return;
            }

            try {
                // Step 1: Fetch book list
                Response<List<HadithBookResponse.HadithBook>> booksResponse = apiService.getBooks().execute();
                if (!booksResponse.isSuccessful() || booksResponse.body() == null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(
                        () -> callback.onError("Failed to fetch Hadith book list"));
                    return;
                }

                List<HadithBookResponse.HadithBook> books = booksResponse.body();
                hadithDao.insertBooks(mapBooksToEntities(books));

                // Step 2: For each book, fetch all hadiths
                for (HadithBookResponse.HadithBook book : books) {
                    try {
                        // Skip this book if hadiths already exist
                        int existing = hadithDao.getHadithCountByBookId(book.id);
                        if (existing > 0) {
                            Log.d("HadithRepository", "Book " + book.id + " already has " + existing + " hadiths. Skipping.");
                            continue;
                        }

                        Response<List<HadithDetailResponse.Hadith>> detailResponse = apiService.getHadithByBook(book.id).execute();
                        if (detailResponse.isSuccessful() && detailResponse.body() != null) {
                            List<HadithDetailResponse.Hadith> hadiths = detailResponse.body();
                            hadithDao.insertHadiths(mapHadithsToEntities(hadiths, book.id));
                        }
                    } catch (Exception e) {
                        // Skip this book on failure, continue with others
                        e.printStackTrace();
                    }
                }

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(true));
            } catch (Exception e) {
                e.printStackTrace();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(
                    () -> callback.onError("Error downloading Hadith: " + e.getMessage()));
            }
        });
    }
}
