package com.mosleemapp.app.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private List<Product> productsAll;
    private Context context;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = new java.util.ArrayList<>(products);
        this.productsAll = new java.util.ArrayList<>(products);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    public void setProducts(List<Product> products) {
        this.products = new java.util.ArrayList<>(products);
        this.productsAll = new java.util.ArrayList<>(products);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        products.clear();
        if (query.isEmpty()) {
            products.addAll(productsAll);
        } else {
            for (Product p : productsAll) {
                if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                    products.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvProductName.setText(product.getName());
        
        java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
        holder.tvProductPrice.setText(format.format(product.getPrice()).replace("Rp", "Rp ").replaceAll(",00$", ""));

        android.graphics.drawable.Drawable placeholder = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_shopping_bag);
        if (placeholder != null) {
            placeholder = androidx.core.graphics.drawable.DrawableCompat.wrap(placeholder).mutate();
            androidx.core.graphics.drawable.DrawableCompat.setTint(placeholder, androidx.core.content.ContextCompat.getColor(context, R.color.primary_green));
        }

        holder.ivProductImage.clearColorFilter(); // Pastikan tidak ada filter nyangkut dari view yang di-recycle

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            android.util.Log.d("ProductAdapter", "Loading image from: " + product.getImageUrl());
            Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(placeholder)
                .error(placeholder)
                .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageDrawable(placeholder);
        }

        View.OnClickListener clickListener = v -> {
            if (product.getUrl() != null && !product.getUrl().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(product.getUrl()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Tidak dapat membuka link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Link tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        };

        holder.btnBuy.setOnClickListener(clickListener);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        Button btnBuy;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}
