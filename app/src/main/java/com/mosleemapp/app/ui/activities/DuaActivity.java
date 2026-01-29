package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.R;
import androidx.lifecycle.ViewModelProvider;
import com.mosleemapp.app.data.local.entity.DuaEntity;
import com.mosleemapp.app.ui.adapters.DuaAdapter;
import com.mosleemapp.app.ui.viewmodel.DuaViewModel;
import com.mosleemapp.app.utils.AdMobUtil;

import java.util.ArrayList;

import com.mosleemapp.app.ui.adapters.CategoryAdapter;
import java.util.ArrayList;
import java.util.List;

public class DuaActivity extends BaseActivity {

    private RecyclerView rvDuaList, rvCategories;
    private DuaAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private DuaViewModel viewModel;
    private FrameLayout flAdPlaceholder;
    private List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSync = findViewById(R.id.btnSync);
        rvDuaList = findViewById(R.id.rvDuaList);
        rvCategories = findViewById(R.id.rvCategories);
        flAdPlaceholder = findViewById(R.id.flAdPlaceholder);

        btnBack.setOnClickListener(v -> finish());
        
        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(DuaViewModel.class);
        
        btnSync.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Syncing data...", android.widget.Toast.LENGTH_SHORT).show();
            viewModel.syncDuas();
        });
        rvDuaList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DuaAdapter(new ArrayList<>());
        rvDuaList.setAdapter(adapter);

        // Categories Setup
        categories.add("All"); // Default
        categoryAdapter = new CategoryAdapter(categories, (category, position) -> {
            categoryAdapter.setSelectedPosition(position);
            filterDuas(category);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(DuaViewModel.class);
        
        // Initial Load (All)
        viewModel.getAllDuas().observe(this, duas -> {
            if (duas != null) {
                adapter.setDuas(duas);
            }
        });

        // Load Categories
        viewModel.getAllCategories().observe(this, cats -> {
            if (cats != null) {
                categories.clear();
                categories.add("All");
                categories.addAll(cats);
                categoryAdapter.notifyDataSetChanged();
            }
        });

        // Load Native Ad
        loadNativeAd();
    }

    private void filterDuas(String category) {
        if (category.equals("All")) {
            viewModel.getAllDuas().observe(this, duas -> adapter.setDuas(duas));
        } else {
            viewModel.getDuasByCategory(category).observe(this, duas -> adapter.setDuas(duas));
        }
    }
    
    private void loadNativeAd() {
        AdMobUtil.initialize(getApplicationContext());
        AdMobUtil.loadBanner(findViewById(R.id.adView));
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
