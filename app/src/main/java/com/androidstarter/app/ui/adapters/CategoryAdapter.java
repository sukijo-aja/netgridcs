package com.androidstarter.app.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.androidstarter.app.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private int selectedPosition = 0;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    public CategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);

        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#1B5E20")); // Dark Green
            holder.tvCategoryName.setTextColor(Color.WHITE);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.tvCategoryName.setTextColor(Color.parseColor("#1B5E20"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
