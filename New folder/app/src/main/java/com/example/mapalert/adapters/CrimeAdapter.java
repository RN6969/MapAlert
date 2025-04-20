package com.example.mapalert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mapalert.R;
import com.example.mapalert.models.Crime;
import java.util.List;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.CrimeViewHolder> {
    private final List<Crime> crimeList;

    public CrimeAdapter(List<Crime> crimeList) {
        this.crimeList = crimeList;
    }

    @NonNull
    @Override
    public CrimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crime, parent, false);
        return new CrimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CrimeViewHolder holder, int position) {
        Crime crime = crimeList.get(position);
        holder.crimeType.setText(crime.getCrimeType());
        holder.description.setText(crime.getDescription());
        holder.date.setText(crime.getDate());
        holder.location.setText(crime.getLocation());
    }

    @Override
    public int getItemCount() {
        return crimeList.size();
    }

    public static class CrimeViewHolder extends RecyclerView.ViewHolder {
        TextView crimeType, description, date, location;

        public CrimeViewHolder(@NonNull View itemView) {
            super(itemView);
            crimeType = itemView.findViewById(R.id.text_crime_type);
            description = itemView.findViewById(R.id.text_description);
            date = itemView.findViewById(R.id.text_date);
            location = itemView.findViewById(R.id.text_location);
        }
    }
}
