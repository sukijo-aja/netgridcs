package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.remote.Responses.AyahResponse;
import com.mosleemapp.app.data.remote.Responses.SurahResponse;
import com.mosleemapp.app.ui.adapters.AyahAdapter;

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
    private List<SurahResponse.Surah> allSurahs = new ArrayList<>();

    private android.os.Handler autoScrollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean isAutoScrolling = false;
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoScrolling && rvAyah != null) {
                rvAyah.scrollBy(0, 2);
                autoScrollHandler.postDelayed(this, 30);
            }
        }
    };

    private void toggleAutoScroll(android.view.MenuItem item) {
        isAutoScrolling = !isAutoScrolling;
        if (isAutoScrolling) {
            item.setIcon(android.R.drawable.ic_media_pause);
            autoScrollHandler.post(autoScrollRunnable);
        } else {
            item.setIcon(android.R.drawable.ic_media_play);
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah_detail);

        rvAyah = findViewById(R.id.rvAyah);
        progressBar = findViewById(R.id.progressBar);
        tvSurahNameTitle = findViewById(R.id.tvSurahNameTitle);
        btnPrevSurah = findViewById(R.id.btnPrevSurah);
        btnNextSurah = findViewById(R.id.btnNextSurah);

        surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, 1);
        surahName = getIntent().getStringExtra(EXTRA_SURAH_NAME);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(surahName != null ? surahName : getString(R.string.quran));
            toolbar.setNavigationOnClickListener(v -> onBackPressed());

            toolbar.inflateMenu(R.menu.menu_surah_detail);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_options) {
                    showOptionsDialog();
                    return true;
                } else if (item.getItemId() == R.id.action_auto_scroll) {
                    toggleAutoScroll(item);
                    return true;
                }
                return false;
            });
            
            android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchItem != null) {
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                if (searchView != null) {
                    searchView.setQueryHint(getString(R.string.search));
                    
                    android.widget.ImageView closeBtn = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
                    if (closeBtn != null) {
                        closeBtn.setOnClickListener(v -> {
                            if (searchView.getQuery().length() == 0) {
                                searchItem.collapseActionView();
                            } else {
                                searchView.setQuery("", false);
                            }
                        });
                    }

                    searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (adapter != null) adapter.filter(query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if (adapter != null) adapter.filter(newText);
                            if (closeBtn != null) {
                                closeBtn.post(() -> closeBtn.setVisibility(android.view.View.VISIBLE));
                            }
                            return false;
                        }
                    });

                    searchItem.setOnActionExpandListener(new android.view.MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(android.view.MenuItem item) {
                            if (closeBtn != null) {
                                closeBtn.post(() -> closeBtn.setVisibility(android.view.View.VISIBLE));
                            }
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(android.view.MenuItem item) {
                            if (adapter != null) adapter.filter("");
                            return true;
                        }
                    });
                }
            }
        }


        loadAllSurahs();
        updateHeader();
        setupRecyclerView();
        setupListeners();
        fetchAyahs(surahNumber);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAd();
    }

    private void loadAd() {
        com.google.android.gms.ads.AdView adView = findViewById(R.id.adView);
        com.mosleemapp.app.utils.AdMobUtil.loadBanner(adView);
    }

    private void updateHeader() {
        if (surahName != null) {
            tvSurahNameTitle.setText(surahName);
            com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(surahName);
            }
        }
        btnPrevSurah.setEnabled(surahNumber > 1);
        btnPrevSurah.setAlpha(surahNumber > 1 ? 1.0f : 0.5f);
        btnNextSurah.setEnabled(surahNumber < 114);
        btnNextSurah.setAlpha(surahNumber < 114 ? 1.0f : 0.5f);
    }
    
    @Override
    public void onBackPressed() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null && toolbar.getMenu() != null) {
            android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchItem != null && searchItem.isActionViewExpanded()) {
                searchItem.collapseActionView();
                return;
            }
        }
        super.onBackPressed();
    }

    private void setupListeners() {
        btnPrevSurah.setOnClickListener(v -> navigateSurah(-1));
        btnNextSurah.setOnClickListener(v -> navigateSurah(1));
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
//                             surahName = com.mosleemapp.app.utils.app.SurahTranslationHelper.getIndonesianTranslation(surahNumber);
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
        if (isAutoScrolling) {
            isAutoScrolling = false;
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null && toolbar.getMenu() != null) {
                android.view.MenuItem autoScrollItem = toolbar.getMenu().findItem(R.id.action_auto_scroll);
                if (autoScrollItem != null) {
                    autoScrollItem.setIcon(android.R.drawable.ic_media_play);
                }
            }
        }
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
                
                // Scroll to last read Ayah if applicable
                com.mosleemapp.app.utils.AppPreference prefs = new com.mosleemapp.app.utils.AppPreference(SurahDetailActivity.this);
                int savedSurah = prefs.getInt("last_read_surah_number", -1);
                int savedAyah = prefs.getInt("last_read_ayah_number", -1);
                
                if (savedSurah == surahNumber && savedAyah > 0) {
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

        com.mosleemapp.app.utils.AppPreference prefs = new com.mosleemapp.app.utils.AppPreference(SurahDetailActivity.this);
        prefs.saveInt("last_read_surah_number", surahNumber);
        prefs.saveString("last_read_surah_name", getIntent().getStringExtra(EXTRA_SURAH_NAME));
        prefs.saveInt("last_read_ayah_number", ayahNumber);
    }

    private void showOptionsDialog() {
        com.mosleemapp.app.utils.app.SettingsManager settingsManager = com.mosleemapp.app.utils.app.SettingsManager.getInstance(this);
        boolean[] checkedItems = {
            settingsManager.isTajweedEnabled(),
            settingsManager.isShowTranslationEnabled()
        };

        String[] options = {
            getString(R.string.show_tajweed_colored_quran),
            getString(R.string.show_translation)
        };

        new android.app.AlertDialog.Builder(this)
            .setTitle(R.string.settings)
            .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                if (which == 0) {
                    settingsManager.setTajweedEnabled(isChecked);
                } else if (which == 1) {
                    settingsManager.setShowTranslationEnabled(isChecked);
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            })
            .setPositiveButton(android.R.string.ok, null)
            .show();
    }
}
