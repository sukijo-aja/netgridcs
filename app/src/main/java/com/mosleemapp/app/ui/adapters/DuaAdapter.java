package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mosleemapp.app.R;
import com.mosleemapp.app.data.local.entity.DuaEntity;
import java.util.List;
import com.mosleemapp.app.utils.LocaleHelper;
import com.mosleemapp.app.utils.ShareUtil;


public class DuaAdapter extends RecyclerView.Adapter<DuaAdapter.DuaViewHolder> {

    private List<DuaEntity> duaList;

    public DuaAdapter(List<DuaEntity> duaList) {
        this.duaList = duaList;
    }

    public void setDuas(List<DuaEntity> duas) {
        this.duaList = duas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DuaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dua, parent, false);
        return new DuaViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DuaViewHolder holder, int position) {
        DuaEntity item = duaList.get(position);
        
        String lang = LocaleHelper.getLanguage(holder.itemView.getContext());
        boolean isIndonesian = "in".equals(lang) || "id".equals(lang);
        
        String title = isIndonesian ? item.titleId : item.titleEn;
        String translation = isIndonesian ? item.translationId : item.translationEn;

        holder.tvTitle.setText(title);
        holder.tvCategory.setText(item.category);
        holder.tvArabic.setText(item.arabic);
        holder.tvLatin.setText(item.latin);
        holder.tvTranslation.setText(translation);
        
        holder.btnShare.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Share Dua");
            builder.setCancelable(true);
            builder.setIcon(R.mipmap.ic_launcher);
            String[] options = {"Share as Text", "Share as Image"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Share Text
                    String shareContent = title + "\n\n" + item.arabic + "\n\n" + item.latin + "\n\n" + translation + "\n\n- Shared via MosleemApp";
                    ShareUtil.shareText(holder.itemView.getContext(), "Share Dua", shareContent);
                } else {
                    // Share Image
                    // Hide share button temporarily for capture
                    holder.btnShare.setVisibility(View.INVISIBLE);
//                    holder.btnShare.setImageResource(R.mipmap.ic_launcher);
                    ShareUtil.shareViewAsImage(holder.itemView.getContext(), holder.itemView, "Share Dua Image");
//                    holder.btnShare.setImageResource(R.drawable.ic_share);
                    holder.btnShare.setVisibility(View.VISIBLE);
                }
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return duaList.size();
    }

    static class DuaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvArabic, tvLatin, tvTranslation;
        ImageView btnShare;

        public DuaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDuaTitle);
            tvCategory = itemView.findViewById(R.id.tvDuaCategory);
            tvArabic = itemView.findViewById(R.id.tvDuaArabic);
            tvLatin = itemView.findViewById(R.id.tvDuaLatin);
            tvTranslation = itemView.findViewById(R.id.tvDuaTranslation);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}
