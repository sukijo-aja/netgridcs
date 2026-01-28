package com.mosleemapp.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrayerAdapter extends RecyclerView.Adapter<PrayerAdapter.PrayerViewHolder> {
    private List<PrayerItem> prayerList = new ArrayList<>();

    public void setPrayerTimes(Map<String, String> timings) {
        prayerList.clear();
        if (timings != null) {
            prayerList.add(new PrayerItem("Imsak", timings.get("Imsak")));
            prayerList.add(new PrayerItem("Fajr", timings.get("Fajr")));
            prayerList.add(new PrayerItem("Sunrise", timings.get("Sunrise")));
            prayerList.add(new PrayerItem("Dhuhr", timings.get("Dhuhr")));
            prayerList.add(new PrayerItem("Asr", timings.get("Asr")));
            prayerList.add(new PrayerItem("Maghrib", timings.get("Maghrib")));
            prayerList.add(new PrayerItem("Sunset", timings.get("Sunset")));
            prayerList.add(new PrayerItem("Isha", timings.get("Isha")));
            prayerList.add(new PrayerItem("Last Third", timings.get("Lastthird")));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prayer, parent, false);
        return new PrayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerViewHolder holder, int position) {
        PrayerItem item = prayerList.get(position);
        
        com.mosleemapp.app.utils.SettingsManager sm = com.mosleemapp.app.utils.SettingsManager.getInstance(holder.itemView.getContext());
        
        // Calculate adjusted time
        int correction = sm.getPrayerTimeCorrection(item.name);
        String displayTime = item.time;
        try {
            // Assumes format "HH:mm" or "HH:mm (TZ)"
            String clearTime = item.time.split(" ")[0];
            String[] parts = clearTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            int totalMinutes = hour * 60 + minute + correction;
            // Handle day wrap? For display, maybe just 0-23h. 
            // If < 0, +24h. If > 24h, -24h.
            while (totalMinutes < 0) totalMinutes += 24 * 60;
            while (totalMinutes >= 24 * 60) totalMinutes -= 24 * 60;
            
            int newHour = totalMinutes / 60;
            int newMinute = totalMinutes % 60;
            displayTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", newHour, newMinute);
            
//            if (correction != 0) {
//                 displayTime += " (" + (correction > 0 ? "+" : "") + correction + ")";
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.tvName.setText(item.name);
        holder.tvTime.setText(displayTime);
        
        // Open adjustment dialog on item click (except switch)
        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                com.mosleemapp.app.ui.dialogs.PrayerAdjustmentDialog dialog = 
                    com.mosleemapp.app.ui.dialogs.PrayerAdjustmentDialog.newInstance(item.name);
                dialog.setOnDismissListener(this::notifyDataSetChanged);
                dialog.show(((androidx.fragment.app.FragmentActivity) v.getContext()).getSupportFragmentManager(), "PrayerAdjustmentDialog");
            }
        });

        // Avoid triggering listener during binding
        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(sm.isPrayerAlarmEnabled(item.name));
        
        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sm.setPrayerAlarmEnabled(item.name, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return prayerList.size();
    }

    static class PrayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;
        com.google.android.material.switchmaterial.SwitchMaterial switchAlarm;

        public PrayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrayerName);
            tvTime = itemView.findViewById(R.id.tvPrayerTime);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }

    private static class PrayerItem {
        String name;
        String time;

        PrayerItem(String name, String time) {
            this.name = name;
            this.time = time;
        }
    }
}
