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

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.remote.Responses.HadithDetailResponse;
import com.mosleemapp.app.ui.adapters.HadithDetailAdapter;

import com.mosleemapp.app.data.repository.HadithRepository;
import com.mosleemapp.app.data.repository.QuranRepository;
import java.util.List;

public class HadithDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_BOOK_NAME = "extra_book_name";

    private RecyclerView rvHadithDetail;
    private HadithDetailAdapter adapter;
    private View loadingLayout;
    private TextView tvBookName;
    private String bookId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hadith_detail);

        rvHadithDetail = findViewById(R.id.rvHadithDetail);
        loadingLayout = findViewById(R.id.loadingLayout);
        tvBookName = findViewById(R.id.tvBookName);

        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        String bookName = getIntent().getStringExtra(EXTRA_BOOK_NAME);

        if (bookName != null) {
            tvBookName.setText(bookName);
        }

        setupRecyclerView();
        if (bookId != null) {
            fetchHadiths(bookId);
        }
    }

    private void setupRecyclerView() {
        adapter = new HadithDetailAdapter();
        rvHadithDetail.setLayoutManager(new LinearLayoutManager(this));
        rvHadithDetail.setAdapter(adapter);
    }

    private void fetchHadiths(String bookId) {
        showLoading(true);
        HadithRepository hadithRepository = new HadithRepository(this);
        hadithRepository.getHadiths(bookId, new QuranRepository.Callback<List<HadithDetailResponse.Hadith>>() {
            @Override
            public void onSuccess(List<HadithDetailResponse.Hadith> data) {
                showLoading(false);
                adapter.setHadiths(data);
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
            loadingLayout.setVisibility(View.VISIBLE);
            rvHadithDetail.setVisibility(View.GONE);
        } else {
            loadingLayout.setVisibility(View.GONE);
            rvHadithDetail.setVisibility(View.VISIBLE);
        }
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

}
