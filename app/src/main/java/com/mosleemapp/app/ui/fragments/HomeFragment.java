package com.mosleemapp.app.ui.fragments;

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
import com.mosleemapp.app.ui.activities.TasbihActivity;
import com.mosleemapp.app.ui.adapters.HomeMenuAdapter;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;

import com.mosleemapp.app.R;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.utils.AdMobUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PrayerViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Home Menu
        List<HomeMenuAdapter.HomeMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Prayer", R.drawable.ic_history));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Tasbih", R.drawable.ic_tasbih));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Quran", R.drawable.ic_quran));
        menuItems.add(new HomeMenuAdapter.HomeMenuItem("Dua", R.drawable.dua));
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
            } else {
                 android.content.Intent intent = new android.content.Intent(getContext(), DetailActivity.class);
                 intent.putExtra("EXTRA_TITLE", item.getTitle());
                 startActivity(intent);
            }
        });
        binding.rvHomeMenu.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvHomeMenu.setAdapter(menuAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PrayerViewModel.class);
        observeViewModel();
        
        loadNativeAd();
    }
    
    private void loadNativeAd() {
        AdMobUtil.loadNativeAd(requireContext(), nativeAd -> {
            if (binding == null) return; // Fragment destroyed
            
            // Inflate Native Ad Layout
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

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
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

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

    private void observeViewModel() {
        viewModel.getNextPrayerName().observe(getViewLifecycleOwner(), name -> {
            binding.tvNextPrayerName.setText(name);
        });

        viewModel.getNextPrayerTimeRemaining().observe(getViewLifecycleOwner(), time -> {
            binding.tvCountdown.setText(time);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
