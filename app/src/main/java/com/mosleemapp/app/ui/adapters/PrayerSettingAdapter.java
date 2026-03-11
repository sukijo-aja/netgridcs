package com.mosleemapp.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mosleemapp.app.R;

import java.util.ArrayList;
import java.util.List;

public class PrayerSettingAdapter extends RecyclerView.Adapter<PrayerSettingAdapter.ViewHolder> {

    public static class PrayerSettingItem {
        public String prayerName;
        public String localizedName;
        public String time;
        public boolean isEnabled;
        public int offset;

        public PrayerSettingItem(String prayerName, String localizedName, String time, boolean isEnabled, int offset) {
            this.prayerName = prayerName;
            this.localizedName = localizedName;
            this.time = time;
            this.isEnabled = isEnabled;
            this.offset = offset;
        }
    }

    public interface OnPrayerSettingInteractionListener {
        void onSwitchToggled(PrayerSettingItem item, boolean isChecked);
        void onOffsetClicked(PrayerSettingItem item, TextView tvOffset);
    }

    private List<PrayerSettingItem> items = new ArrayList<>();
    private final OnPrayerSettingInteractionListener listener;

    public PrayerSettingAdapter(OnPrayerSettingInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PrayerSettingItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prayer_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrayerSettingItem item = items.get(position);
        
        String timeStr = item.time != null ? item.time.split(" ")[0] : "00:00";
        holder.switchPrayer.setText(item.localizedName + " (" + timeStr + ")");
        
        // Remove listener before changing state to avoid false triggers
        holder.switchPrayer.setOnCheckedChangeListener(null);
        holder.switchPrayer.setChecked(item.isEnabled);
        
        holder.switchPrayer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onSwitchToggled(item, isChecked);
            }
        });

        if (item.offset == 0) {
            holder.tvOffsetPrayer.setText("On Time");
        } else {
            holder.tvOffsetPrayer.setText(item.offset + " min before");
        }

        holder.tvOffsetPrayer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOffsetClicked(item, holder.tvOffsetPrayer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        SwitchMaterial switchPrayer;
        TextView tvOffsetPrayer;

        ViewHolder(View itemView) {
            super(itemView);
            switchPrayer = itemView.findViewById(R.id.switchPrayer);
            tvOffsetPrayer = itemView.findViewById(R.id.tvOffsetPrayer);
        }
    }
}
