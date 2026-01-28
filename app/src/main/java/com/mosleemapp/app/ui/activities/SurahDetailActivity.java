package com.mosleemapp.app.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;
import java.util.List;
import com.mosleemapp.app.data.models.AyahResponse;
import com.mosleemapp.app.data.remote.QuranApiService;
import com.mosleemapp.app.data.remote.RetrofitClient;
import com.mosleemapp.app.ui.adapters.AyahAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mosleemapp.app.data.repository.QuranRepository;

public class SurahDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SURAH_NUMBER = "extra_surah_number";
    public static final String EXTRA_SURAH_NAME = "extra_surah_name";

    private RecyclerView rvAyah;
    private AyahAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvSurahNameTitle;
    private int surahNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah_detail);

        rvAyah = findViewById(R.id.rvAyah);
        progressBar = findViewById(R.id.progressBar);
        tvSurahNameTitle = findViewById(R.id.tvSurahNameTitle);

        surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, 1);
        String surahName = getIntent().getStringExtra(EXTRA_SURAH_NAME);

        if (surahName != null) {
            tvSurahNameTitle.setText(surahName);
        }

        setupRecyclerView();
        fetchAyahs(surahNumber);
        saveLastRead();
    }

    private void setupRecyclerView() {
        adapter = new AyahAdapter();
        rvAyah.setLayoutManager(new LinearLayoutManager(this));
        rvAyah.setAdapter(adapter);
    }

    private void fetchAyahs(int number) {
        showLoading(true);
        QuranRepository quranRepository = new QuranRepository(this);
        quranRepository.getAyahs(number, new QuranRepository.Callback<List<AyahResponse.Ayah>>() {
            @Override
            public void onSuccess(List<AyahResponse.Ayah> data) {
                showLoading(false);
                
                // Remove Bismillah from the first Ayah if it's not Surah Al-Fatiha (1) or At-Tawbah (9)
                if (data != null && !data.isEmpty() && surahNumber != 1 && surahNumber != 9) {
                    AyahResponse.Ayah firstAyah = data.get(0);
                    // Bismillah string with possible variations or exact match
                    String bismillah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ";
                    if (firstAyah.text.startsWith(bismillah)) {
                        firstAyah.text = firstAyah.text.replaceFirst(bismillah, "").trim();
                    }
                }
                
                adapter.setAyahs(data);
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(SurahDetailActivity.this, getString(R.string.error_prefix, message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            rvAyah.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            rvAyah.setVisibility(View.VISIBLE);
        }
    }

    private void saveLastRead() {
        // Simple SharedPrefs implementation for "Last Read"
        SharedPreferences prefs = getSharedPreferences("MoslemAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("last_read_surah_number", surahNumber);
        editor.putString("last_read_surah_name", getIntent().getStringExtra(EXTRA_SURAH_NAME));
        editor.apply();
    }
}
