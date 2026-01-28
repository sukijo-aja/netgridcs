package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mosleemapp.app.data.models.SurahResponse;
import com.mosleemapp.app.databinding.ItemSurahBinding;
import java.util.ArrayList;
import java.util.List;

public class SurahAdapter extends RecyclerView.Adapter<SurahAdapter.SurahViewHolder> {

    private List<SurahResponse.Surah> surahList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SurahResponse.Surah surah);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSurahs(List<SurahResponse.Surah> surahs) {
        this.surahList = surahs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SurahViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSurahBinding binding = ItemSurahBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SurahViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SurahViewHolder holder, int position) {
        SurahResponse.Surah surah = surahList.get(position);
        holder.bind(surah);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(surah);
            }
        });
    }

    @Override
    public int getItemCount() {
        return surahList.size();
    }

    static class SurahViewHolder extends RecyclerView.ViewHolder {
        private final ItemSurahBinding binding;

        public SurahViewHolder(@NonNull ItemSurahBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SurahResponse.Surah surah) {
            binding.tvNumber.setText(String.valueOf(surah.number));
            binding.tvEnglishName.setText(surah.englishName);
            binding.tvEnglishNameTranslation.setText(surah.englishNameTranslation);
            binding.tvNameArabic.setText(surah.name);
            binding.tvVerses.setText(surah.numberOfAyahs + " Verses");

            float fontSize = com.mosleemapp.app.utils.SettingsManager.getInstance(itemView.getContext()).getArabicFontSize();
            binding.tvNameArabic.setTextSize(fontSize);
        }
    }
}
