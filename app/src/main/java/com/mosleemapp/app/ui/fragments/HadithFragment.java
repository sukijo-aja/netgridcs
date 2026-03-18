package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.remote.Responses.HadithBookResponse;
import com.mosleemapp.app.databinding.FragmentHadithBinding;
import com.mosleemapp.app.ui.activities.HadithDetailActivity;
import com.mosleemapp.app.ui.adapters.HadithBookAdapter;
import android.content.Intent;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.List;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mosleemapp.app.utils.AdMobUtil;

import com.mosleemapp.app.data.repository.HadithRepository;
import com.mosleemapp.app.data.repository.QuranRepository;

public class HadithFragment extends Fragment {

    private FragmentHadithBinding binding;
    private HadithBookAdapter adapter;
    private List<HadithBookResponse.HadithBook> originalBookList = new ArrayList<>();
    private HadithRepository hadithRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHadithBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        adapter = new HadithBookAdapter();
        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(getContext(), HadithDetailActivity.class);
            intent.putExtra(HadithDetailActivity.EXTRA_BOOK_ID, book.id);
            intent.putExtra(HadithDetailActivity.EXTRA_BOOK_NAME, book.name);
            startActivity(intent);
        });
        binding.rvHadith.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHadith.setAdapter(adapter);

        binding.rvHadith.setAdapter(adapter);

        setupSearchView();
        
        hadithRepository = new HadithRepository(requireContext());

        // Fetch Data
        fetchBooks();
        
        loadNativeAd();
    }

    private void fetchBooks() {
        hadithRepository.getBooks(new QuranRepository.Callback<List<HadithBookResponse.HadithBook>>() {
             @Override
             public void onSuccess(List<HadithBookResponse.HadithBook> data) {
                 originalBookList = data;
                 adapter.setBooks(originalBookList);
             }

             @Override
             public void onError(String message) {
                 if (getContext() != null) {
                     Toast.makeText(getContext(), getString(R.string.check_your_internet_connection_and_try_again), Toast.LENGTH_SHORT).show();
                     android.util.Log.e("HadithFragment", "Error fetching surahs: " + message);
                 }
             }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBooks(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBooks(newText);
                return false;
            }
        });
    }

    private void filterBooks(String query) {
        List<HadithBookResponse.HadithBook> filteredList = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(originalBookList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (HadithBookResponse.HadithBook book : originalBookList) {
                if (book.name.toLowerCase().contains(filterPattern)) {
                    filteredList.add(book);
                }
            }
        }
        adapter.setBooks(filteredList);
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
