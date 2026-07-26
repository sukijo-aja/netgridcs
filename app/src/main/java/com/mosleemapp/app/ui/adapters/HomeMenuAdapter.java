package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;


import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.ViewHolder> {

    private final List<HomeMenuItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HomeMenuItem item);
    }

    public HomeMenuAdapter(List<HomeMenuItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeMenuItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.ivIcon.setImageResource(item.getIconRes());
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivMenuIcon);
            tvTitle = itemView.findViewById(R.id.tvMenuTitle);
        }
    }

    public static class HomeMenuItem {
        private String id;
        private String title;
        private int iconRes;

        public HomeMenuItem(String id, String title, int iconRes) {
            this.id = id;
            this.title = title;
            this.iconRes = iconRes;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public int getIconRes() {
            return iconRes;
        }
    }
}
