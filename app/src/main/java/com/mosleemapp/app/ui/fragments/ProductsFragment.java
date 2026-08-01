package com.mosleemapp.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.model.Product;
import com.mosleemapp.app.ui.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Products");
            // Navigation click listener will be overridden below if searchItem exists
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
            
            toolbar.inflateMenu(R.menu.menu_products);
            android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchItem != null) {
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                searchView.setQueryHint("Cari produk...");
                
                toolbar.setNavigationOnClickListener(v -> {
                    if (searchItem.isActionViewExpanded()) {
                        searchItem.collapseActionView();
                    } else {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });

                android.widget.ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
                if (closeButton != null) {
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

        rvProducts = view.findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new ProductAdapter(requireContext(), new ArrayList<>());
        rvProducts.setAdapter(adapter);

        fetchProducts();
    }

    private void fetchProducts() {
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/sukijo-aja/repodata/main/data/muslimapp/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();

        com.mosleemapp.app.data.remote.services.ProductApiService apiService = 
                retrofit.create(com.mosleemapp.app.data.remote.services.ProductApiService.class);

        apiService.getProducts().enqueue(new retrofit2.Callback<List<Product>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Product>> call, retrofit2.Response<List<Product>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProducts(response.body());
                } else {
                    android.widget.Toast.makeText(requireContext(), "Gagal memuat produk", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Product>> call, Throwable t) {
                if (!isAdded()) return;
                android.widget.Toast.makeText(requireContext(), "Gagal terhubung ke server", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
