package com.androidstarter.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidstarter.app.R;
import com.androidstarter.app.data.local.entity.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationEntity> notifications = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NotificationEntity notification, String formattedDate);
    }

    public NotificationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<NotificationEntity> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationEntity notification = notifications.get(position);
        holder.tvTitle.setText(notification.title);
        holder.tvMessage.setText(notification.message);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(notification.timestamp)));
        
        if (notification.messageId != null && !notification.messageId.isEmpty()) {
            holder.tvMessageId.setText("ID: " + notification.messageId);
            holder.tvMessageId.setVisibility(View.VISIBLE);
        } else {
            holder.tvMessageId.setVisibility(View.GONE);
        }
        
        if (notification.isRead) {
            holder.vUnreadIndicator.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.7f);
            androidx.core.view.ViewCompat.setBackgroundTintList(holder.itemView, android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        } else {
            holder.vUnreadIndicator.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
            androidx.core.view.ViewCompat.setBackgroundTintList(holder.itemView, android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0FDF4")));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification, sdf.format(new Date(notification.timestamp)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTimestamp;
        TextView tvMessageId;
        View vUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvMessageId = itemView.findViewById(R.id.tvMessageId);
            vUnreadIndicator = itemView.findViewById(R.id.vUnreadIndicator);
        }
    }
}
