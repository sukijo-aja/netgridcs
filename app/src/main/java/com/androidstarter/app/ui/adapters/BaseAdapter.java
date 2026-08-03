package com.androidstarter.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic RecyclerView Adapter with DiffUtil, click listeners, and type safety.
 * Eliminates boilerplate for simple-to-moderate list implementations.
 *
 * Usage:
 *   BaseAdapter<User> adapter = new BaseAdapter<>(R.layout.item_user, (holder, user, position) -> {
 *       TextView name = holder.getView(R.id.tvName);
 *       name.setText(user.getName());
 *   });
 *   adapter.setOnItemClickListener((user, position) -> openDetail(user));
 *   recyclerView.setAdapter(adapter);
 *   adapter.submitList(userList);
 */
public class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {

    private final int layoutResId;
    private final Binder<T> binder;
    private List<T> items = new ArrayList<>();
    private OnItemClickListener<T> clickListener;
    private OnItemLongClickListener<T> longClickListener;
    private DiffCallback<T> diffCallback;

    public BaseAdapter(@LayoutRes int layoutResId, Binder<T> binder) {
        this.layoutResId = layoutResId;
        this.binder = binder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        binder.bind(holder, item, position);

        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(item, position));
        }
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(item, position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Submit a new list with DiffUtil calculation. */
    public void submitList(List<T> newItems) {
        if (diffCallback != null) {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() { return items.size(); }

                @Override
                public int getNewListSize() { return newItems.size(); }

                @Override
                public boolean areItemsTheSame(int oldPos, int newPos) {
                    return diffCallback.areItemsTheSame(items.get(oldPos), newItems.get(newPos));
                }

                @Override
                public boolean areContentsTheSame(int oldPos, int newPos) {
                    return diffCallback.areContentsTheSame(items.get(oldPos), newItems.get(newPos));
                }
            });
            items = new ArrayList<>(newItems);
            result.dispatchUpdatesTo(this);
        } else {
            items = new ArrayList<>(newItems);
            notifyDataSetChanged();
        }
    }

    /** Set the list directly without DiffUtil. */
    public void setItems(List<T> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    /** Add a single item to the end. */
    public void addItem(T item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    /** Remove item at position. */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    /** Get item at position. */
    public T getItem(int position) {
        return items.get(position);
    }

    /** Get the full list. */
    public List<T> getItems() {
        return items;
    }

    /** Set click listener. */
    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.clickListener = listener;
    }

    /** Set long click listener. */
    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.longClickListener = listener;
    }

    /** Set DiffUtil callback for efficient list updates. */
    public void setDiffCallback(DiffCallback<T> callback) {
        this.diffCallback = callback;
    }

    // ---- ViewHolder ----

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        /** Get any view by ID from the item layout. */
        @SuppressWarnings("unchecked")
        public <V extends View> V getView(int id) {
            return (V) itemView.findViewById(id);
        }
    }

    // ---- Interfaces ----

    public interface Binder<T> {
        void bind(ViewHolder holder, T item, int position);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item, int position);
    }

    public interface OnItemLongClickListener<T> {
        void onItemLongClick(T item, int position);
    }

    public interface DiffCallback<T> {
        boolean areItemsTheSame(T oldItem, T newItem);
        boolean areContentsTheSame(T oldItem, T newItem);
    }
}
