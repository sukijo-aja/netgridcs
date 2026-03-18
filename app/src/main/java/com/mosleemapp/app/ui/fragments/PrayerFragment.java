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

import com.mosleemapp.app.databinding.FragmentPrayerBinding;
import com.mosleemapp.app.ui.adapters.PrayerAdapter;
import com.mosleemapp.app.ui.viewmodel.PrayerViewModel;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.R;

import java.util.HashMap;
import java.util.Map;

public class PrayerFragment extends Fragment {

    private FragmentPrayerBinding binding;
    private PrayerViewModel viewModel;
    private PrayerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup UI
        adapter = new PrayerAdapter();
        binding.rvPrayerTimes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPrayerTimes.setAdapter(adapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(PrayerViewModel.class);
        
        // Observe Data
        observeViewModel();
        
        loadNativeAd();
    }

    private void observeViewModel() {
        viewModel.getPrayerTimes().observe(getViewLifecycleOwner(), prayerTimeEntity -> {
            if (prayerTimeEntity != null) {
                Map<String, String> timings = new HashMap<>();
                timings.put("Fajr", prayerTimeEntity.fajr);
                timings.put("Sunrise", prayerTimeEntity.sunrise);
                timings.put("Dhuhr", prayerTimeEntity.dhuhr);
                timings.put("Asr", prayerTimeEntity.asr);
                timings.put("Maghrib", prayerTimeEntity.maghrib);
                timings.put("Sunset", prayerTimeEntity.sunset);
                timings.put("Isha", prayerTimeEntity.isha);
                timings.put("Imsak", prayerTimeEntity.imsak);
                timings.put("Lastthird", prayerTimeEntity.lastThird);
                adapter.setPrayerTimes(timings);
            }
        });
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
