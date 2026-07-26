package com.mosleemapp.app.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.model.Product;
import com.mosleemapp.app.ui.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends BaseActivity {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Products");
            toolbar.setNavigationOnClickListener(v -> finish());
            
            toolbar.inflateMenu(R.menu.menu_products);
            android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
            if (searchItem != null) {
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                searchView.setQueryHint("Cari produk...");
                searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (adapter != null) {
                            adapter.filter(newText);
                        }
                        return true;
                    }
                });
            }
        }

        rvProducts = findViewById(R.id.rvProducts);
        // Using GridLayoutManager for 2 columns like a marketplace
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new ProductAdapter(this, new ArrayList<>());
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
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProducts(response.body());
                } else {
                    android.widget.Toast.makeText(ProductsActivity.this, "Gagal memuat produk", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Product>> call, Throwable t) {
                android.widget.Toast.makeText(ProductsActivity.this, "Gagal terhubung ke server", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
