package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.models.DuaItem;
import java.util.List;

public class DuaAdapter extends RecyclerView.Adapter<DuaAdapter.DuaViewHolder> {

    private List<DuaItem> duaList;

    public DuaAdapter(List<DuaItem> duaList) {
        this.duaList = duaList;
    }

    @NonNull
    @Override
    public DuaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dua, parent, false);
        return new DuaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DuaViewHolder holder, int position) {
        DuaItem item = duaList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvArabic.setText(item.getArabic());
        holder.tvLatin.setText(item.getLatin());
        holder.tvTranslation.setText(item.getTranslation());
    }

    @Override
    public int getItemCount() {
        return duaList.size();
    }

    static class DuaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArabic, tvLatin, tvTranslation;

        public DuaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDuaTitle);
            tvArabic = itemView.findViewById(R.id.tvDuaArabic);
            tvLatin = itemView.findViewById(R.id.tvDuaLatin);
            tvTranslation = itemView.findViewById(R.id.tvDuaTranslation);
        }
    }
}
