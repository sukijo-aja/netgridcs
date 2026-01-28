package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mosleemapp.app.R;

import java.util.List;

public class TasbihAdapter extends RecyclerView.Adapter<TasbihAdapter.ViewHolder> {

    private final List<TasbihItem> items;
    private final OnTasbihInteractionListener listener;
    private int selectedPosition = 0;

    public interface OnTasbihInteractionListener {
        void onItemSelect(TasbihItem item, int position);
        void onReset(TasbihItem item, int position);
    }

    public TasbihAdapter(List<TasbihItem> items, OnTasbihInteractionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(int position) {
        notifyItemChanged(position);
    }
    
    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    public TasbihItem getSelectedItem() {
        if (selectedPosition >= 0 && selectedPosition < items.size()) {
            return items.get(selectedPosition);
        }
        return null;
    }
    
    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tasbih, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TasbihItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCount.setText(String.valueOf(item.getCount()));

        // Highlight selection
        if (selectedPosition == position) {
            holder.container.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.background_light));
            holder.tvName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.primary_green));
        } else {
            holder.container.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.tvName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onItemSelect(item, position);
        });

        holder.btnReset.setOnClickListener(v -> listener.onReset(item, position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView container;
        TextView tvName, tvCount;
        ImageView btnReset;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvTasbihName);
            tvCount = itemView.findViewById(R.id.tvTasbihCount);
            btnReset = itemView.findViewById(R.id.btnItemReset);
        }
    }

    public static class TasbihItem {
        private String id;
        private String name;
        private int count;

        public TasbihItem(String id, String name, int count) {
            this.id = id;
            this.name = name;
            this.count = count;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}
