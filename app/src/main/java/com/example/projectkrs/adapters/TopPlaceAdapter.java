package com.example.projectkrs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectkrs.R;
import com.example.projectkrs.model.PlaceWithCount;

import java.util.List;

public class TopPlaceAdapter extends RecyclerView.Adapter<TopPlaceAdapter.TopPlaceViewHolder> {

    private List<PlaceWithCount> placeList;

    public TopPlaceAdapter(List<PlaceWithCount> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public TopPlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_place, parent, false);
        return new TopPlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopPlaceViewHolder holder, int position) {
        PlaceWithCount place = placeList.get(position);
        holder.tvPlaceName.setText(place.getName());
        holder.tvCount.setText("Lankyta: " + place.getCount());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class TopPlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceName, tvCount;
        TopPlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvCount = itemView.findViewById(R.id.tvCount);
        }
    }
}
