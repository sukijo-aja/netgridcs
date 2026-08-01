package com.mosleemapp.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mosleemapp.app.R;
import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder> {

    private List<CalendarEvent> events;
    private Context context;

    public CalendarEventAdapter(Context context, List<CalendarEvent> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.tvEventDate.setText(event.getHijriDate());
        holder.tvEventName.setText(event.getEventName());
        holder.tvEventMasehiDate.setText(event.getMasehiDate());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventDate, tvEventName, tvEventMasehiDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventMasehiDate = itemView.findViewById(R.id.tvEventMasehiDate);
        }
    }

    public static class CalendarEvent {
        private String hijriDate;
        private String eventName;
        private String masehiDate;

        public CalendarEvent(String hijriDate, String eventName, String masehiDate) {
            this.hijriDate = hijriDate;
            this.eventName = eventName;
            this.masehiDate = masehiDate;
        }

        public String getHijriDate() { return hijriDate; }
        public String getEventName() { return eventName; }
        public String getMasehiDate() { return masehiDate; }
    }
}
