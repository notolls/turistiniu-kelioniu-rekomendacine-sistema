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
import com.bumptech.glide.request.RequestOptions;
import com.example.projectkrs.R;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private Context context;
    private List<Place> placesList;

    // konstruktorius
    public ImageSliderAdapter(Context context, List<Place> placesList) {
        this.context = context;
        this.placesList = placesList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_image_slider.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Duomenų susiejimas su rodiniais
        Place place = placesList.get(position);

        // Load paveikslėlį naudojant Glide library
        List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
            // Photo metadata yra
            PhotoMetadata photoMetadata = photoMetadataList.get(0); // gauti pirma paveikslėlį
            String photoReference = photoMetadata.zzb(); // gauti photo reference
            String apiKey = context.getString(R.string.places_api_key); // API raktas iš strings.xml
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + apiKey;
            Glide.with(holder.itemView)
                    .load(photoUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.imageView);
        }

        // Nustatyti pavadinimą
        holder.placeNameTextView.setText(place.getName());
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView placeNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slider_item);
            placeNameTextView = itemView.findViewById(R.id.imageNameTextView);
        }
    }
    public void updateData(List<Place> newData) {
        placesList.clear();
        placesList.addAll(newData);
        notifyDataSetChanged();
    }

}
