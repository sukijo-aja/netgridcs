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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.models.AyahResponse;
import com.mosleemapp.app.data.models.SurahResponse;
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
    public static final String EXTRA_SURAH = "extra_surah";

    private RecyclerView rvAyah;
    private AyahAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvSurahNameTitle;
    private int surahNumber;
    private String surahName;

    private ImageButton btnPrevSurah, btnNextSurah;
    private Spinner spinnerAyahs;
    private List<SurahResponse.Surah> allSurahs = new ArrayList<>();
    private boolean isSpinnerInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah_detail);

        rvAyah = findViewById(R.id.rvAyah);
        progressBar = findViewById(R.id.progressBar);
        tvSurahNameTitle = findViewById(R.id.tvSurahNameTitle);
        btnPrevSurah = findViewById(R.id.btnPrevSurah);
        btnNextSurah = findViewById(R.id.btnNextSurah);
        spinnerAyahs = findViewById(R.id.spinnerAyahs);

        surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, 1);
        surahName = getIntent().getStringExtra(EXTRA_SURAH_NAME);

        loadAllSurahs();
        updateHeader();
        setupRecyclerView();
        setupListeners();
        fetchAyahs(surahNumber);
    }
    
    private void updateHeader() {
        if (surahName != null) {
            tvSurahNameTitle.setText(surahName);
        }
        btnPrevSurah.setEnabled(surahNumber > 1);
        btnPrevSurah.setAlpha(surahNumber > 1 ? 1.0f : 0.5f);
        btnNextSurah.setEnabled(surahNumber < 114);
        btnNextSurah.setAlpha(surahNumber < 114 ? 1.0f : 0.5f);
    }
    
    private void setupListeners() {
        btnPrevSurah.setOnClickListener(v -> navigateSurah(-1));
        btnNextSurah.setOnClickListener(v -> navigateSurah(1));
        
        spinnerAyahs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialized) {
                    RecyclerView.LayoutManager layoutManager = rvAyah.getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) {
                        ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, 0);
                    }
                }
                isSpinnerInitialized = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void navigateSurah(int offset) {
        int nextSurahNumber = surahNumber + offset;
        if (nextSurahNumber >= 1 && nextSurahNumber <= 114) {
            surahNumber = nextSurahNumber;
            surahName = "Surah " + surahNumber; // Fallback
            if (allSurahs != null && !allSurahs.isEmpty()) {
                for (SurahResponse.Surah s : allSurahs) {

                    if (s.number == surahNumber) {
//                        // try indonesian
//                        String currentLang = com.mosleemapp.app.utils.LocaleHelper.getLanguage(this);
//                        if ("in".equals(currentLang)) {
//                             surahName = com.mosleemapp.app.utils.SurahTranslationHelper.getIndonesianTranslation(surahNumber);
//                        } else {
                            surahName = s.englishName;
//                        }
//                        surahName = s.name;
                        break;
                    }
                }
            }
            updateHeader();
            fetchAyahs(surahNumber);
        }
    }
    
    private void loadAllSurahs() {
        QuranRepository quranRepository = new QuranRepository(this);
        quranRepository.getSurahs(new QuranRepository.Callback<List<SurahResponse.Surah>>() {
            @Override
            public void onSuccess(List<SurahResponse.Surah> data) {
                allSurahs = data;
                // update current name if we just loaded it
                if (surahName == null || surahName.startsWith("Surah ")) {
                   navigateSurah(0); // refresh logic
                }
            }
            @Override
            public void onError(String message) {}
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                
                // Populate Spinner
                isSpinnerInitialized = false;
                List<String> ayahStrings = new ArrayList<>();
                if (data != null) {
                    for (int i = 0; i < data.size(); i++) {
                        ayahStrings.add("Ayah " + (i + 1));
                    }
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(SurahDetailActivity.this, android.R.layout.simple_spinner_item, ayahStrings);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAyahs.setAdapter(spinnerAdapter);
                
                // Scroll to last read Ayah if applicable
                SharedPreferences prefs = getSharedPreferences("MoslemAppPrefs", MODE_PRIVATE);
                int savedSurah = prefs.getInt("last_read_surah_number", -1);
                int savedAyah = prefs.getInt("last_read_ayah_number", -1);
                
                if (savedSurah == surahNumber && savedAyah > 0) {
                    spinnerAyahs.setSelection(savedAyah - 1);
                    rvAyah.post(() -> {
                        RecyclerView.LayoutManager layoutManager = rvAyah.getLayoutManager();
                        if (layoutManager instanceof LinearLayoutManager) {
                            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(savedAyah - 1, 0);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(getApplicationContext(), getString(R.string.check_your_internet_connection_and_try_again), Toast.LENGTH_SHORT).show();
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
        int firstVisibleItemPosition = 0;
        RecyclerView.LayoutManager layoutManager = rvAyah.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        int ayahNumber = firstVisibleItemPosition + 1; // Ayah numbers are 1-based

        SharedPreferences prefs = getSharedPreferences("MoslemAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("last_read_surah_number", surahNumber);
        editor.putString("last_read_surah_name", getIntent().getStringExtra(EXTRA_SURAH_NAME));
        editor.putInt("last_read_ayah_number", ayahNumber);
        editor.apply();
    }
}
