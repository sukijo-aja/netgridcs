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
            prayerList.add(new PrayerItem("Fajr", timings.get("Fajr")));
            prayerList.add(new PrayerItem("Dhuhr", timings.get("Dhuhr")));
            prayerList.add(new PrayerItem("Asr", timings.get("Asr")));
            prayerList.add(new PrayerItem("Maghrib", timings.get("Maghrib")));
            prayerList.add(new PrayerItem("Isha", timings.get("Isha")));
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
        holder.tvName.setText(item.name);
        holder.tvTime.setText(item.time);
    }

    @Override
    public int getItemCount() {
        return prayerList.size();
    }

    static class PrayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;

        public PrayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrayerName);
            tvTime = itemView.findViewById(R.id.tvPrayerTime);
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
