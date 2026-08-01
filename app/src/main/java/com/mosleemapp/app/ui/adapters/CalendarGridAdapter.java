package com.mosleemapp.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mosleemapp.app.R;
import java.util.List;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.ViewHolder> {

    private List<CalendarDay> days;
    private Context context;

    public CalendarGridAdapter(Context context, List<CalendarDay> days) {
        this.context = context;
        this.days = days;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        if (day.getHijriDay() == 0) {
            holder.tvHijriDay.setText("");
            holder.tvMasehiDay.setText("");
        } else {
            holder.tvHijriDay.setText(String.valueOf(day.getHijriDay()));
            holder.tvMasehiDay.setText(String.valueOf(day.getMasehiDay()));

            if (day.isToday()) {
                holder.tvHijriDay.setTextColor(Color.parseColor("#00695C")); // primary_green
                holder.tvMasehiDay.setTextColor(Color.parseColor("#00695C"));
                holder.itemView.setBackgroundResource(R.drawable.bg_rounded_white); // Can make this a specific "today" bg later
            } else {
                holder.tvHijriDay.setTextColor(Color.parseColor("#212121")); // text_primary
                holder.tvMasehiDay.setTextColor(Color.parseColor("#757575")); // text_secondary
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHijriDay, tvMasehiDay;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHijriDay = itemView.findViewById(R.id.tvHijriDay);
            tvMasehiDay = itemView.findViewById(R.id.tvMasehiDay);
        }
    }

    public static class CalendarDay {
        private int hijriDay;
        private int masehiDay;
        private boolean isToday;

        public CalendarDay(int hijriDay, int masehiDay, boolean isToday) {
            this.hijriDay = hijriDay;
            this.masehiDay = masehiDay;
            this.isToday = isToday;
        }

        public int getHijriDay() { return hijriDay; }
        public int getMasehiDay() { return masehiDay; }
        public boolean isToday() { return isToday; }
    }
}
