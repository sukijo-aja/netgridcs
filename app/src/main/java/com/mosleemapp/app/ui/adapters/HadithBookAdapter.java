package com.mosleemapp.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mosleemapp.app.data.models.HadithBookResponse;
import com.mosleemapp.app.databinding.ItemHadithBookBinding;
import java.util.ArrayList;
import java.util.List;

public class HadithBookAdapter extends RecyclerView.Adapter<HadithBookAdapter.BookViewHolder> {

    private List<HadithBookResponse.HadithBook> bookList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HadithBookResponse.HadithBook book);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<HadithBookResponse.HadithBook> books) {
        this.bookList = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHadithBookBinding binding = ItemHadithBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        HadithBookResponse.HadithBook book = bookList.get(position);
        holder.bind(book);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemHadithBookBinding binding;

        public BookViewHolder(@NonNull ItemHadithBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HadithBookResponse.HadithBook book) {
            binding.tvBookName.setText(book.name);
            binding.tvAvailable.setText(book.available + " Hadiths");
        }
    }
}
