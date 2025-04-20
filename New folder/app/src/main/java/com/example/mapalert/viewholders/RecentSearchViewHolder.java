package com.example.mapalert.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapalert.R;

public class RecentSearchViewHolder extends RecyclerView.ViewHolder {
    public ImageView ivMapSnippet;
    public TextView tvSearchText;

    public RecentSearchViewHolder(@NonNull View itemView) {
        super(itemView);
        ivMapSnippet = itemView.findViewById(R.id.ivMapSnippet);
        tvSearchText = itemView.findViewById(R.id.tvSearchText);
    }
}
