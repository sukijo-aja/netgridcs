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

import com.mosleemapp.app.databinding.FragmentHomeBinding;

import com.mosleemapp.app.ui.activities.SurahDetailActivity;

import android.content.Intent;

import com.mosleemapp.app.ui.adapters.HomeMenuAdapter;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;

import com.mosleemapp.app.R;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.utils.AppPreference;
import com.mosleemapp.app.utils.app.SettingsManager;

import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PrayerViewModel prayerViewModel;
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
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("quran", getString(R.string.quran), R.drawable.ic_quran));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("hadith", getString(R.string.hadith), R.drawable.ic_book));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("khutbah", getString(R.string.menu_khutbah), R.drawable.ic_menu_book));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("calendar", getString(R.string.menu_calendar), R.drawable.ic_calendar));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("tasbih", getString(R.string.menu_tasbih), R.drawable.ic_tasbih));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("dua", getString(R.string.menu_dua), R.drawable.dua));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("prayer", getString(R.string.menu_prayer), R.drawable.ic_history));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("tracker", getString(R.string.menu_tracker), R.drawable.ic_history));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("qibla", getString(R.string.menu_qibla), R.drawable.ic_kaaba));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("our_apps", getString(R.string.menu_our_apps), R.drawable.ic_menu));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("share_app", "Share App", R.drawable.ic_share));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("remove_ads", getString(R.string.menu_remove_ads), R.drawable.ic_premium));

        HomeMenuAdapter menuAdapter = new HomeMenuAdapter(menuItems, item -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottom_navigation);

            switch (item.getId()) {
                case "quran":
                    bottomNav.setSelectedItemId(R.id.nav_quran);
                    break;
                case "settings":
                    bottomNav.setSelectedItemId(R.id.nav_settings);
                    break;
                case "hadith":
                    bottomNav.setSelectedItemId(R.id.nav_hadith);
                    break;
                case "khutbah":
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new KhutbahFragment());
                    break;
                case "calendar":
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new CalendarFragment());
                    break;
                case "tasbih":
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new TasbihFragment());
                    break;
                case "dua":
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new DuaFragment());
                    break;
                case "remove_ads":
                    com.mosleemapp.app.utils.AppPreference appPreference = new com.mosleemapp.app.utils.AppPreference(getContext());
                    if (appPreference.getString("UID", "").isEmpty()) {
                        android.widget.Toast.makeText(getContext(), "Please login to continue", android.widget.Toast.LENGTH_SHORT).show();
                        startActivity(new android.content.Intent(getContext(), com.mosleemapp.app.ui.activities.LoginActivity.class));
                    } else {
                        startActivity(new android.content.Intent(getContext(), com.mosleemapp.app.ui.activities.PurchaseActivity.class));
                    }
                    break;
                case "tracker":
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new PrayerTrackerFragment());
                    break;
                case "qibla":
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new QiblaFragment());
                    break;
                case "prayer":
                    com.mosleemapp.app.ui.dialogs.PrayerSettingsBottomSheet bottomSheet = new com.mosleemapp.app.ui.dialogs.PrayerSettingsBottomSheet();
                    bottomSheet.show(getParentFragmentManager(), com.mosleemapp.app.ui.dialogs.PrayerSettingsBottomSheet.TAG);
                    break;
                case "our_apps":
                    com.mosleemapp.app.utils.AppPreference appPref = new com.mosleemapp.app.utils.AppPreference(getContext());
                    String ourAppsUrl = appPref.getString("our_apps", "");
                    if (!ourAppsUrl.isEmpty()) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(ourAppsUrl));
                            startActivity(intent);
                        } catch (Exception e) {
                            android.widget.Toast.makeText(getContext(), "Invalid link format", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.widget.Toast.makeText(getContext(), "Link not available yet", android.widget.Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "share_app":
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out MosleemApp: https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
                    sendIntent.setType("text/plain");
                    Intent shareIntent = Intent.createChooser(sendIntent, "Share App via");
                    startActivity(shareIntent);
                    break;
                default:
                    DetailFragment detailFragment = new DetailFragment();
                    android.os.Bundle args = new android.os.Bundle();
                    args.putString("EXTRA_TITLE", item.getTitle());
                    detailFragment.setArguments(args);
                    ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(detailFragment);
                    break;
            }

        });

        int spanCount = 4;

        binding.rvHomeMenu.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        binding.rvHomeMenu.setAdapter(menuAdapter);

        if (!appPreference.getString("city","").isEmpty()){
            binding.tvCity.setText(appPreference.getString("city",""));
            binding.tvCity.setVisibility(View.VISIBLE);
        }

        prayerViewModel = new ViewModelProvider(requireActivity()).get(PrayerViewModel.class);
        observeViewModel();

        binding.products.setOnClickListener(v -> {
            ((com.mosleemapp.app.MainActivity) requireActivity()).loadFragmentWithBackStack(new ProductsFragment());
        });
    }
        
    @Override
    public void onResume() {
        super.onResume();
        updateLastRead();
        setupGreetingAndDate();
        setupAppName();
       logAllSharedPreferences();
    }

    private void setupAppName() {
        if (binding == null || getContext() == null) return;
        com.mosleemapp.app.utils.AppPreference prefs = new com.mosleemapp.app.utils.AppPreference(requireContext());
        String customAppName = prefs.getString("app_name", getString(R.string.app_name));
        binding.tvTitle.setText(customAppName);
    }

    private void logAllSharedPreferences() {
        if (getContext() == null) return;
        
        android.util.Log.d("SharedPreferencesLog", "--- START PREFS LOG ---");
        
        android.util.Log.d("SharedPreferencesLog", "[config]");
        com.mosleemapp.app.utils.AppPreference configPrefs = new com.mosleemapp.app.utils.AppPreference(requireContext());
        for (java.util.Map.Entry<String, ?> entry : configPrefs.getAll().entrySet()) {
            if (entry.getKey().equals("lat") || entry.getKey().equals("lon")) {
                android.util.Log.d("SharedPreferencesLog", entry.getKey() + " = " + Double.longBitsToDouble((long) entry.getValue()));
            } else {
                android.util.Log.d("SharedPreferencesLog", entry.getKey() + " = " + entry.getValue());
            }

        }

        android.util.Log.d("SharedPreferencesLog", "--- END PREFS LOG ---");
    }

    @SuppressLint("SetTextI18n")
    private void setupGreetingAndDate() {
        if (binding == null) return;
        
        binding.tvGreeting.setText(R.string.assalamu_alaikum);

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
        com.mosleemapp.app.utils.AppPreference prefs = new com.mosleemapp.app.utils.AppPreference(requireContext());
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
        }
    }

    private void loadNativeAd() {
        SettingsManager settingsManager = SettingsManager.getInstance(requireContext());
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

    private int getPrayerNameResource(String name) {
        if (name == null) return -1;
        switch (name.toLowerCase()) {
            case "fajr": return R.string.fajr;
            case "dhuhr": return R.string.dhuhr;
            case "asr": return R.string.asr;
            case "maghrib": return R.string.maghrib;
            case "isha": return R.string.isha;
            default: return -1;
        }
    }

    private void observeViewModel() {
        prayerViewModel.getNextPrayerName().observe(getViewLifecycleOwner(), name -> {
            int resId = getPrayerNameResource(name);
            if (resId != -1) {
                binding.tvNextPrayerName.setText(resId);
            } else {
                binding.tvNextPrayerName.setText(name);
            }
        });

        prayerViewModel.getNextPrayerTime().observe(getViewLifecycleOwner(), time -> {
            binding.tvNextPrayerTime.setText(time);
        });

        prayerViewModel.getNextPrayerTimeRemaining().observe(getViewLifecycleOwner(), time -> {
            binding.tvCountdown.setText(time);
        });

        prayerViewModel.getCityName().observe(getViewLifecycleOwner(), city -> {
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
