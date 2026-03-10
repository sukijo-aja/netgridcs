package com.mosleemapp.app.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mosleemapp.app.databinding.FragmentHomeBinding;
import com.mosleemapp.app.ui.activities.DetailActivity;
import com.mosleemapp.app.ui.activities.DuaActivity;
import com.mosleemapp.app.ui.activities.QiblaActivity;
import com.mosleemapp.app.ui.activities.TasbihActivity;
import com.mosleemapp.app.ui.activities.SurahDetailActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.mosleemapp.app.ui.adapters.HomeMenuAdapter;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;

import com.mosleemapp.app.R;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.utils.AppPreference;

import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PrayerViewModel viewModel;
    AppPreference appPreference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appPreference = new com.mosleemapp.app.utils.AppPreference(getContext());

        // Setup Home Menu
        List<HomeMenuAdapter.HomeMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Prayer", R.drawable.ic_history));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Tasbih", R.drawable.ic_tasbih));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Quran", R.drawable.ic_quran));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Dua", R.drawable.dua));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Tracker", R.drawable.ic_history));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Qibla", R.drawable.ic_kaaba));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Remove Ads", R.drawable.ic_premium));
//        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Favorites", R.drawable.ic_favorite));

        HomeMenuAdapter menuAdapter = new HomeMenuAdapter(menuItems, item -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottom_navigation);

            if (item.getTitle().equals("Prayer")) {
                bottomNav.setSelectedItemId(R.id.nav_prayer);
            } else if (item.getTitle().equals("Quran")) {
                bottomNav.setSelectedItemId(R.id.nav_quran);
            } else if (item.getTitle().equals("Settings")) {
                bottomNav.setSelectedItemId(R.id.nav_settings);
            } else if (item.getTitle().equals("Hadith")) {
                bottomNav.setSelectedItemId(R.id.nav_hadith);
            } else if (item.getTitle().equals("Tasbih")) {
                startActivity(new android.content.Intent(getContext(), TasbihActivity.class));
            } else if (item.getTitle().equals("Dua")) {
                startActivity(new android.content.Intent(getContext(), DuaActivity.class));
            } else if (item.getTitle().equals("Remove Ads")) {
                com.mosleemapp.app.utils.AppPreference appPreference = new com.mosleemapp.app.utils.AppPreference(getContext());
                if (appPreference.getString("UID", "").isEmpty()) {
                    android.widget.Toast.makeText(getContext(), "Please login to continue", android.widget.Toast.LENGTH_SHORT).show();
                    startActivity(new android.content.Intent(getContext(), com.mosleemapp.app.ui.activities.LoginActivity.class));
                } else {
                    startActivity(new android.content.Intent(getContext(), com.mosleemapp.app.ui.activities.PurchaseActivity.class));
                }
            } else if (item.getTitle().equals("Tracker")) {
                startActivity(new android.content.Intent(getContext(), com.mosleemapp.app.ui.activities.PrayerTrackerActivity.class));
            } else if (item.getTitle().equals("Qibla")) {
                startActivity(new android.content.Intent(getContext(), QiblaActivity.class));
            } else {
                android.content.Intent intent = new android.content.Intent(getContext(), DetailActivity.class);
                intent.putExtra("EXTRA_TITLE", item.getTitle());
                startActivity(intent);
            }

        });
        int spanCount = 4;
//        if (getResources().getConfiguration().screenWidthDp >= 600) {
//            spanCount = 4;
//        } else if (getResources().getConfiguration().screenWidthDp >= 360) {
//            spanCount = 3;
//        } else {
//            spanCount = 2;
//        }
        binding.rvHomeMenu.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        binding.rvHomeMenu.setAdapter(menuAdapter);
        if (!appPreference.getString("city","").isEmpty()){
            binding.tvCity.setText(appPreference.getString("city",""));
            binding.tvCity.setVisibility(View.VISIBLE);
        }

        viewModel = new ViewModelProvider(requireActivity()).get(PrayerViewModel.class);
        observeViewModel();
    }
        
    @Override
    public void onResume() {
        super.onResume();
        loadNativeAd();
        updateLastRead();
        setupGreetingAndDate();
    }

    @SuppressLint("SetTextI18n")
    private void setupGreetingAndDate() {
        if (binding == null) return;
        
        binding.tvGreeting.setText(R.string.assalamu_alaikum);
        binding.tvUserName.setText("Mosleem");
        
        try {
            ULocale locale = new ULocale("en@calendar=islamic");
            Calendar calendar = Calendar.getInstance(locale);
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, locale);
            String formattedDate = df.format(calendar.getTime());
            binding.tvHijriDate.setText(formattedDate);
            
            java.text.SimpleDateFormat masehiFormat = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());
            binding.tvMasehiDate.setText(masehiFormat.format(new java.util.Date()));

            if (!appPreference.getString("USER_NAME", "").isEmpty()) {
                binding.tvUserName.setText(appPreference.getString("USER_NAME", ""));
                binding.tvUserName.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            binding.tvHijriDate.setText("");
            binding.tvMasehiDate.setText("");
        }
    }

    private void updateLastRead() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("MoslemAppPrefs", Context.MODE_PRIVATE);
        int lastReadSurahNumber = prefs.getInt("last_read_surah_number", -1);
        String lastReadSurahName = prefs.getString("last_read_surah_name", "");
        int lastReadAyahNumber = prefs.getInt("last_read_ayah_number", -1);

        if (lastReadSurahNumber != -1) {
            binding.cardLastRead.setVisibility(View.VISIBLE);

            String displayText = lastReadSurahName;
            if (lastReadAyahNumber > 0) {
                displayText += " - Ayah " + lastReadAyahNumber;
            }
            binding.tvLastReadSurah.setText(displayText);
            
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

    private void loadNativeAd() {
        com.mosleemapp.app.utils.SettingsManager settingsManager = com.mosleemapp.app.utils.SettingsManager.getInstance(requireContext());
        if (settingsManager.isPremium()) {
            if (binding != null) {
                binding.flAdPlaceholder.removeAllViews();
            }
            return;
        }

        AdMobUtil.loadNativeAd(requireContext(), nativeAd -> {
            if (binding == null) return; // Fragment destroyed
            if (settingsManager.isPremium()) return;

            NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_native, null);
            populateNativeAdView(nativeAd, adView);
            
            binding.flAdPlaceholder.removeAllViews();
            binding.flAdPlaceholder.addView(adView);
        });
    }
    
    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
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
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    private void observeViewModel() {
        viewModel.getNextPrayerName().observe(getViewLifecycleOwner(), name -> {
            binding.tvNextPrayerName.setText(name);
        });

        viewModel.getNextPrayerTimeRemaining().observe(getViewLifecycleOwner(), time -> {
            binding.tvCountdown.setText(time);
        });

        viewModel.getCityName().observe(getViewLifecycleOwner(), city -> {
            if (city != null) {
                binding.tvCity.setText(city);
                binding.tvCity.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
