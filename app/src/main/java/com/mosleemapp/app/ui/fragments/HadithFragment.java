package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mosleemapp.app.data.models.HadithBookResponse;
import com.mosleemapp.app.data.remote.HadithApiService;
import com.mosleemapp.app.databinding.FragmentHadithBinding;
import com.mosleemapp.app.ui.activities.HadithDetailActivity;
import com.mosleemapp.app.ui.adapters.HadithBookAdapter;
import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.List;

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
                     Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
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
}
