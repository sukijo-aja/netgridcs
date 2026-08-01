package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.entity.DuaEntity;
import com.mosleemapp.app.ui.adapters.DuaAdapter;
import com.mosleemapp.app.ui.viewmodel.DuaViewModel;
import com.mosleemapp.app.utils.AdMobUtil;
import com.mosleemapp.app.ui.adapters.CategoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class DuaFragment extends Fragment {

    private RecyclerView rvDuaList, rvCategories;
    private DuaAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private DuaViewModel viewModel;
    private FrameLayout flAdPlaceholder;
    private List<String> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dua, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDuaList = view.findViewById(R.id.rvDuaList);
        rvCategories = view.findViewById(R.id.rvCategories);
        flAdPlaceholder = view.findViewById(R.id.flAdPlaceholder);

        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Daily Dua");
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
            toolbar.inflateMenu(R.menu.menu_dua);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_sync) {
                    android.widget.Toast.makeText(requireContext(), "Syncing data...", android.widget.Toast.LENGTH_SHORT).show();
                    if (viewModel != null) {
                        viewModel.syncDuas();
                    }
                    return true;
                }
                return false;
            });
        }
        
        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(DuaViewModel.class);
        
        rvDuaList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DuaAdapter(new ArrayList<>());
        rvDuaList.setAdapter(adapter);

        // Categories Setup
        categories.add("All"); // Default
        categoryAdapter = new CategoryAdapter(categories, (category, position) -> {
            categoryAdapter.setSelectedPosition(position);
            filterDuas(category);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(DuaViewModel.class);
        
        // Initial Load (All)
        viewModel.getAllDuas().observe(getViewLifecycleOwner(), duas -> {
            if (duas != null) {
                adapter.setDuas(duas);
            }
        });

        // Load Categories
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), cats -> {
            if (cats != null) {
                categories.clear();
                categories.add("All");
                categories.addAll(cats);
                categoryAdapter.notifyDataSetChanged();
            }
        });

        // Load Native Ad
        loadNativeAd(view);
    }

    private void filterDuas(String category) {
        if (category.equals("All")) {
            viewModel.getAllDuas().observe(getViewLifecycleOwner(), duas -> adapter.setDuas(duas));
        } else {
            viewModel.getDuasByCategory(category).observe(getViewLifecycleOwner(), duas -> adapter.setDuas(duas));
        }
    }
    
    private void loadNativeAd(View view) {
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
