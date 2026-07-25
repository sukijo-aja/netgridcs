package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.remote.Responses.AyahResponse;
import com.mosleemapp.app.utils.app.SettingsManager;
import com.mosleemapp.app.utils.ShareUtil;
import com.mosleemapp.app.utils.app.TajweedHelper;

import java.util.ArrayList;
import java.util.List;

public class AyahAdapter extends RecyclerView.Adapter<AyahAdapter.AyahViewHolder> {

    private List<AyahResponse.Ayah> ayahs = new ArrayList<>();

    public void setAyahs(List<AyahResponse.Ayah> ayahs) {
        this.ayahs = ayahs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AyahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ayah, parent, false);
        return new AyahViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AyahViewHolder holder, int position) {
        AyahResponse.Ayah ayah = ayahs.get(position);
        holder.bind(ayah);
    }

    @Override
    public int getItemCount() {
        return ayahs.size();
    }

    static class AyahViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAyahNumber;
        private TextView tvAyahText;
        private TextView tvAyahTranslation;
        private ImageView btnShare;

        public AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAyahNumber = itemView.findViewById(R.id.tvAyahNumber);
            tvAyahText = itemView.findViewById(R.id.tvAyahText);
            tvAyahTranslation = itemView.findViewById(R.id.tvAyahTranslation);
            btnShare = itemView.findViewById(R.id.btnShare);
        }

        public void bind(AyahResponse.Ayah ayah) {
            tvAyahNumber.setText(String.valueOf(ayah.numberInSurah));
            
            boolean isTajweed = SettingsManager.getInstance(itemView.getContext()).isTajweedEnabled();
            if (isTajweed && ayah.textTajweed != null) {
                tvAyahText.setText(TajweedHelper.parseTajweed(ayah.textTajweed));
            } else {
                tvAyahText.setText(ayah.text);
            }
            
            boolean showTranslation = SettingsManager.getInstance(itemView.getContext()).isShowTranslationEnabled();
            if (showTranslation) {
                tvAyahTranslation.setVisibility(View.VISIBLE);
                tvAyahTranslation.setText(ayah.translation);
            } else {
                tvAyahTranslation.setVisibility(View.GONE);
            }

            float fontSize = SettingsManager.getInstance(itemView.getContext()).getArabicFontSize();
            tvAyahText.setTextSize(fontSize);

            btnShare.setOnClickListener(v -> {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(v.getContext());
                builder.setTitle("Share Qur'an");
                builder.setCancelable(true);
                builder.setIcon(R.mipmap.ic_launcher);
                String[] options = {"Share as Text", "Share as Image"};
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        String shareContent = ayah.text + "("+ ayah.numberInSurah +")\n"+ ayah.translation + "\n\n Shared via MosleemApp";
                        ShareUtil.shareText(v.getContext(), "Share Quran", shareContent);
                    } else {
                        tvAyahNumber.setVisibility(View.GONE);
                        btnShare.setVisibility(View.GONE);
                        ShareUtil.shareViewAsImage(v.getContext(), itemView, "Share Qur'an Image");
                        btnShare.setVisibility(View.VISIBLE);
                        tvAyahNumber.setVisibility(View.VISIBLE);
                    }
                });
                builder.show();

            });
        }
    }
}
