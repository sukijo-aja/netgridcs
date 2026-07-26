package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
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
import com.mosleemapp.app.data.remote.Responses.SurahResponse;
import com.mosleemapp.app.databinding.FragmentQuranBinding;
import com.mosleemapp.app.ui.activities.SurahDetailActivity;
import com.mosleemapp.app.ui.adapters.SurahAdapter;
import android.content.Intent;

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

        com.google.android.material.appbar.MaterialToolbar toolbar = binding.getRoot().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.quran);
            toolbar.inflateMenu(R.menu.menu_quran);
            android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchItem != null) {
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                if (searchView != null) {
                    searchView.setQueryHint(getString(R.string.search_surah));
                    
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

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            filterSurahs(query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            filterSurahs(newText);
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
                            filterSurahs("");
                            return true;
                        }
                    });
                }
            }

            toolbar.setNavigationOnClickListener(v -> {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                    requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    requireActivity().onBackPressed();
                }
            });
            
            if (searchItem != null) {
                requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (searchItem.isActionViewExpanded()) {
                            searchItem.collapseActionView();
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });
            }
        }

        // Setup RecyclerView
        adapter = new SurahAdapter();
        adapter.setOnItemClickListener(surah -> {
            Intent intent = new Intent(getContext(), SurahDetailActivity.class);
            intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NUMBER, surah.number);
            intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NAME, surah.englishName);
            intent.putExtra(SurahDetailActivity.EXTRA_SURAH, surah.name);
            startActivity(intent);
        });
        binding.rvQuran.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQuran.setAdapter(adapter);

        binding.rvQuran.setAdapter(adapter);


        
        quranRepository = new QuranRepository(requireContext());

        // Fetch Data
        fetchSurahs();
        
        loadAd();
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    Toast.makeText(getContext(), getString(R.string.check_your_internet_connection_and_try_again), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("QuranFragment", "Error fetching surahs: " + message);
                }
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

    private void loadAd() {
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
