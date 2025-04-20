package com.example.mapalert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapalert.R;
import com.example.mapalert.models.RecentSearch;
import com.example.mapalert.viewholders.RecentSearchViewHolder;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchViewHolder> {

    private final List<RecentSearch> searches;
    private final OnSearchClickListener listener;

    public interface OnSearchClickListener {
        void onSearchClick(RecentSearch search);
    }

    public RecentSearchAdapter(List<RecentSearch> searches, OnSearchClickListener listener) {
        this.searches = searches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecentSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_search, parent, false);
        return new RecentSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentSearchViewHolder holder, int position) {
        RecentSearch search = searches.get(position);
        holder.tvSearchText.setText(search.getSearchText());

        Glide.with(holder.itemView.getContext())
                .load(search.getMapImageUrl())
                .placeholder(R.drawable.placeholder_map) // Temporary placeholder
                .into(holder.ivMapSnippet);

        holder.itemView.setOnClickListener(v -> listener.onSearchClick(search));
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }
}

