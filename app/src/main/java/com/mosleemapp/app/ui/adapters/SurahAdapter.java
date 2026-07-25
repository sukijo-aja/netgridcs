package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;
import com.mosleemapp.app.data.remote.Responses.SurahResponse;
import com.mosleemapp.app.databinding.ItemSurahBinding;
import com.mosleemapp.app.utils.app.SettingsManager;
import com.mosleemapp.app.utils.app.SurahTranslationHelper;

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
            
            String currentLang = com.mosleemapp.app.utils.LocaleHelper.getLanguage(itemView.getContext());
            if ("in".equals(currentLang)) {
                binding.tvEnglishNameTranslation.setText(SurahTranslationHelper.getIndonesianTranslation(surah.number));
            } else {
                binding.tvEnglishNameTranslation.setText(surah.englishNameTranslation);
            }
            
            // Remove "سورة" prefix (with or without diacritical marks/harakat)
            String arabicName = surah.name != null
                    ? surah.name.replaceAll("س[\\u0610-\\u065F\\u0670\\u06D6-\\u06ED]*و[\\u0610-\\u065F\\u0670\\u06D6-\\u06ED]*ر[\\u0610-\\u065F\\u0670\\u06D6-\\u06ED]*ة[\\u0610-\\u065F\\u0670\\u06D6-\\u06ED]*\\s*", "").trim()
                    : "";
            binding.tvNameArabic.setText(arabicName);
            binding.tvVerses.setText(" ("+surah.numberOfAyahs + " "+ itemView.getContext().getString(R.string.verses) +")");

            float fontSize = SettingsManager.getInstance(itemView.getContext()).getArabicFontSize();
            binding.tvNameArabic.setTextSize(fontSize);
        }
    }
}
