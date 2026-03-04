package com.example.projectkrs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectkrs.R;
import com.example.projectkrs.model.PlaceRecommendation;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PlaceRecommendation place);
    }

    private final Context context;
    private final List<PlaceRecommendation> recommendations;
    private final OnItemClickListener listener;

    public RecommendationAdapter(Context context, List<PlaceRecommendation> recommendations, OnItemClickListener listener) {
        this.context = context;
        this.recommendations = recommendations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceRecommendation place = recommendations.get(position);

        holder.textViewName.setText(place.getName());

        // Nuotrauka iš photoUrl
        if (place.getPhotoUrl() != null && !place.getPhotoUrl().isEmpty()) {
            Glide.with(context).load(place.getPhotoUrl()).into(holder.imageView);
        } else {
            // Jei nuotrauka nerasta, rodyti default
            Glide.with(context).load(R.drawable.no_image_placeholder).into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(place));
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recoImage);
            textViewName = itemView.findViewById(R.id.recoName);
        }
    }
}