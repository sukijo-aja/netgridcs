package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.models.SurahResponse;
import com.mosleemapp.app.data.remote.QuranApiService;
import com.mosleemapp.app.databinding.FragmentQuranBinding;
import com.mosleemapp.app.ui.activities.SurahDetailActivity;
import com.mosleemapp.app.ui.adapters.SurahAdapter;
import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.List;

import android.widget.Button;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.utils.AdMobUtil;

import com.mosleemapp.app.data.repository.QuranRepository;

public class QuranFragment extends Fragment {

    private FragmentQuranBinding binding;
    private SurahAdapter adapter;
    private List<SurahResponse.Surah> originalSurahList = new ArrayList<>();
    private QuranRepository quranRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQuranBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        adapter = new SurahAdapter();
        adapter.setOnItemClickListener(surah -> {
            Intent intent = new Intent(getContext(), SurahDetailActivity.class);
            intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NUMBER, surah.number);
            intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NAME, surah.englishName);
            startActivity(intent);
        });
        binding.rvQuran.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQuran.setAdapter(adapter);

        binding.rvQuran.setAdapter(adapter);

        setupSearchView();
        
        quranRepository = new QuranRepository(requireContext());

        // Fetch Data
        fetchSurahs();
        
        loadNativeAd();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLastRead();
    }

    private void updateLastRead() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoslemAppPrefs", Context.MODE_PRIVATE);
        int lastReadSurahNumber = prefs.getInt("last_read_surah_number", -1);
        String lastReadSurahName = prefs.getString("last_read_surah_name", "");

        if (lastReadSurahNumber != -1) {
            binding.cardLastRead.setVisibility(View.VISIBLE);
            binding.tvLastReadSurah.setText(lastReadSurahName);
            binding.cardLastRead.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), SurahDetailActivity.class);
                intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NUMBER, lastReadSurahNumber);
                intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NAME, lastReadSurahName);
                startActivity(intent);
            });
        } else {
            binding.cardLastRead.setVisibility(View.GONE);
        }
    }

    private void fetchSurahs() {
        quranRepository.getSurahs(new QuranRepository.Callback<List<SurahResponse.Surah>>() {
            @Override
            public void onSuccess(List<SurahResponse.Surah> data) {
                 originalSurahList = data;
                 adapter.setSurahs(originalSurahList);
            }

            @Override
            public void onError(String message) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.error_prefix, message), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSurahs(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSurahs(newText);
                return false;
            }
        });
    }

    private void filterSurahs(String query) {
        List<SurahResponse.Surah> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(originalSurahList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (SurahResponse.Surah surah : originalSurahList) {
                if (surah.englishName.toLowerCase().contains(filterPattern) || 
                    surah.englishNameTranslation.toLowerCase().contains(filterPattern)) {
                    filteredList.add(surah);
                }
            }
        }
        adapter.setSurahs(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadNativeAd() {
        AdMobUtil.initialize(getContext());
        AdMobUtil.loadBanner(binding.adView);
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }
}
