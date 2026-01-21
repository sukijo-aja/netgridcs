package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.models.AyahResponse;

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

        public AyahViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAyahNumber = itemView.findViewById(R.id.tvAyahNumber);
            tvAyahText = itemView.findViewById(R.id.tvAyahText);
            tvAyahTranslation = itemView.findViewById(R.id.tvAyahTranslation);
        }

        public void bind(AyahResponse.Ayah ayah) {
            tvAyahNumber.setText(String.valueOf(ayah.numberInSurah));
            tvAyahText.setText(ayah.text);
            tvAyahTranslation.setText(ayah.translation);

            float fontSize = com.mosleemapp.app.utils.SettingsManager.getInstance(itemView.getContext()).getArabicFontSize();
            tvAyahText.setTextSize(fontSize);
        }
    }
}
