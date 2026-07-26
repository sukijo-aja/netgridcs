package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.remote.Responses.HadithDetailResponse;
import com.mosleemapp.app.utils.ShareUtil;
import com.mosleemapp.app.utils.app.SettingsManager;

import java.util.ArrayList;
import java.util.List;

public class HadithDetailAdapter extends RecyclerView.Adapter<HadithDetailAdapter.HadithViewHolder> {

    private List<HadithDetailResponse.Hadith> hadithList = new ArrayList<>();
    private List<HadithDetailResponse.Hadith> allHadiths = new ArrayList<>();

    public void setHadiths(List<HadithDetailResponse.Hadith> hadiths) {
        this.hadithList = new ArrayList<>(hadiths);
        this.allHadiths = new ArrayList<>(hadiths);
        notifyDataSetChanged();
    }
    
    public void filter(String query) {
        hadithList.clear();
        if (query == null || query.isEmpty()) {
            hadithList.addAll(allHadiths);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (HadithDetailResponse.Hadith hadith : allHadiths) {
                boolean matchesNumber = String.valueOf(hadith.number).contains(filterPattern);
                boolean matchesArab = hadith.arab != null && hadith.arab.toLowerCase().contains(filterPattern);
                boolean matchesTranslation = hadith.id != null && hadith.id.toLowerCase().contains(filterPattern);
                
                if (matchesNumber || matchesArab || matchesTranslation) {
                    hadithList.add(hadith);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HadithViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hadith_detail, parent, false);
        return new HadithViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HadithViewHolder holder, int position) {
        HadithDetailResponse.Hadith hadith = hadithList.get(position);
        holder.bind(hadith);
    }

    @Override
    public int getItemCount() {
        return hadithList.size();
    }

    static class HadithViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHadithNumber;
        private TextView tvHadithArabic;
        private TextView tvHadithTranslation;
        private ImageView btnShare;

        public HadithViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHadithNumber = itemView.findViewById(R.id.tvHadithNumber);
            tvHadithArabic = itemView.findViewById(R.id.tvHadithArabic);
            tvHadithTranslation = itemView.findViewById(R.id.tvHadithTranslation);
            btnShare = itemView.findViewById(R.id.btnShare);
        }

        public void bind(HadithDetailResponse.Hadith hadith) {
            tvHadithNumber.setText("Hadith No. " + hadith.number);
            tvHadithArabic.setText(hadith.arab);
            tvHadithTranslation.setText(hadith.id);

            String currentLang = com.mosleemapp.app.utils.LocaleHelper.getLanguage(itemView.getContext());
            boolean isArabic = "ar".equals(currentLang);
            if (isArabic) {
                tvHadithTranslation.setVisibility(View.GONE);
            } else {
                tvHadithTranslation.setVisibility(View.VISIBLE);
            }

            float fontSize = SettingsManager.getInstance(itemView.getContext()).getArabicFontSize();
            tvHadithArabic.setTextSize(fontSize);

            btnShare.setOnClickListener(v -> {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Share Hadist");
                builder.setCancelable(true);
                builder.setIcon(R.mipmap.ic_launcher);
                String[] options = {"Share as Text", "Share as Image"};
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        String shareContent = hadith.arab+ "("+ hadith.number +")\n"+ hadith.id + "\n\n Shared via MosleemApp";
                        ShareUtil.shareText(v.getContext(), "Share Hadist", shareContent);
                    } else {
                        btnShare.setVisibility(View.INVISIBLE);
                        ShareUtil.shareViewAsImage(itemView.getContext(), itemView, "Share Hadist Image");
                        btnShare.setVisibility(View.VISIBLE);
                    }
                });
                builder.show();

            });
        }
    }
}
