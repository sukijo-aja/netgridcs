package com.mosleemapp.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.entity.KhutbahEntity;

import java.util.ArrayList;
import java.util.List;

public class KhutbahAdapter extends RecyclerView.Adapter<KhutbahAdapter.KhutbahViewHolder> {

    private List<KhutbahEntity> khutbahs;
    private List<KhutbahEntity> khutbahsAll;
    private Context context;

    public KhutbahAdapter(Context context, List<KhutbahEntity> khutbahs) {
        this.context = context;
        this.khutbahs = new ArrayList<>(khutbahs);
        this.khutbahsAll = new ArrayList<>(khutbahs);
    }

    @NonNull
    @Override
    public KhutbahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_khutbah, parent, false);
        return new KhutbahViewHolder(view);
    }

    public void setKhutbahs(List<KhutbahEntity> khutbahs) {
        this.khutbahs = new ArrayList<>(khutbahs);
        this.khutbahsAll = new ArrayList<>(khutbahs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        khutbahs.clear();
        if (query.isEmpty()) {
            khutbahs.addAll(khutbahsAll);
        } else {
            for (KhutbahEntity k : khutbahsAll) {
                if (k.title.toLowerCase().contains(query.toLowerCase()) || 
                    (k.khotib != null && k.khotib.toLowerCase().contains(query.toLowerCase()))) {
                    khutbahs.add(k);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull KhutbahViewHolder holder, int position) {
        KhutbahEntity khutbah = khutbahs.get(position);
        holder.tvKhutbahTitle.setText(khutbah.title);
        holder.tvKhotib.setText(khutbah.khotib != null ? khutbah.khotib : "-");
        holder.tvDescription.setText(khutbah.description != null ? khutbah.description : "");

        View.OnClickListener clickListener = v -> {
            if (context instanceof com.mosleemapp.app.MainActivity) {
                ((com.mosleemapp.app.MainActivity) context).loadFragmentWithBackStack(
                    com.mosleemapp.app.ui.fragments.KhutbahDetailFragment.newInstance(khutbah.id)
                );
            }
        };

        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return khutbahs.size();
    }

    public static class KhutbahViewHolder extends RecyclerView.ViewHolder {
        TextView tvKhutbahTitle;
        TextView tvKhotib;
        TextView tvDescription;

        public KhutbahViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKhutbahTitle = itemView.findViewById(R.id.tvKhutbahTitle);
            tvKhotib = itemView.findViewById(R.id.tvKhotib);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
