package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.entity.KhutbahEntity;
import com.mosleemapp.app.data.repository.KhutbahRepository;
import com.mosleemapp.app.ui.adapters.KhutbahAdapter;
import com.mosleemapp.app.utils.AppPreference;

import java.util.ArrayList;
import java.util.List;

public class KhutbahFragment extends Fragment {

    private RecyclerView rvKhutbah;
    private KhutbahAdapter adapter;
    private KhutbahRepository repository;
    private android.widget.ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_khutbah, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.menu_khutbah));
            // Navigation click listener will be overridden below if searchItem exists
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
            
            toolbar.inflateMenu(R.menu.menu_products);
            android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchItem != null) {
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                searchView.setQueryHint("Cari khutbah...");
                
                toolbar.setNavigationOnClickListener(v -> {
                    if (searchItem.isActionViewExpanded()) {
                        searchItem.collapseActionView();
                    } else {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });

                android.widget.ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
                if (closeButton != null) {
                    // Make sure it's visible initially when expanded
                    searchItem.setOnActionExpandListener(new android.view.MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(android.view.MenuItem item) {
                            closeButton.post(() -> closeButton.setVisibility(View.VISIBLE));
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(android.view.MenuItem item) {
                            return true;
                        }
                    });

                    closeButton.setOnClickListener(v -> {
                        searchView.setQuery("", false);
                        searchView.clearFocus();
                        searchItem.collapseActionView();
                    });
                }

                searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (closeButton != null && newText.isEmpty()) {
                            // Android hides the close button when text is empty, force it back to visible
                            closeButton.post(() -> closeButton.setVisibility(View.VISIBLE));
                        }
                        if (adapter != null) {
                            adapter.filter(newText);
                        }
                        return true;
                    }
                });
            }
        }

        rvKhutbah = view.findViewById(R.id.rvKhutbah);
        progressBar = view.findViewById(R.id.progressBar);
        rvKhutbah.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new KhutbahAdapter(requireContext(), new ArrayList<>());
        rvKhutbah.setAdapter(adapter);

        repository = new KhutbahRepository(requireActivity().getApplication());
        
        AppPreference prefs = new AppPreference(requireContext());
        boolean isUpdateRequired = prefs.getBoolean("UPDATE_KHUTBAH_REQUIRED", true);

        repository.getAllKhutbahs().observe(getViewLifecycleOwner(), khutbahs -> {
            if (khutbahs != null && !khutbahs.isEmpty()) {
                adapter.setKhutbahs(khutbahs);
                progressBar.setVisibility(View.GONE);
                
                // If it's empty during first load, we still want to fetch
            } else if (!isUpdateRequired) {
                // Database is empty but update not required? Force update.
                syncData(prefs);
            }
        });

        if (isUpdateRequired) {
            syncData(prefs);
        }
    }

    private void syncData(AppPreference prefs) {
        progressBar.setVisibility(View.VISIBLE);
        repository.syncKhutbahData(
            () -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        prefs.saveBoolean("UPDATE_KHUTBAH_REQUIRED", false);
                        android.widget.Toast.makeText(requireContext(), "Data khutbah berhasil diperbarui", android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            },
            () -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        android.widget.Toast.makeText(requireContext(), "Gagal memperbarui data khutbah", android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
    }
}
