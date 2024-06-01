package com.example.projectkrs.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectkrs.model.PlaceWithDistance;
import com.example.projectkrs.R;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class PostAdapter<T> extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<T> itemsList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PostAdapter(List<T> itemsList) {
        this.itemsList = itemsList;
    }

    public void updateData(List<T> newData) {
        itemsList.clear();
        itemsList.addAll(newData);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        T item = itemsList.get(position);
        if (item instanceof Place) {
            holder.bind((Place) item);
        } else if (item instanceof PlaceWithDistance) {
            holder.bind((PlaceWithDistance) item);
        }
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTextView;

        public PostViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);

            // Click listeneris
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        // Bind metodas vietai be atstumo (HomeFragment)
        public void bind(Place place) {
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // Photo metadata yra
                PhotoMetadata photoMetadata = photoMetadataList.get(0); // Gauti pirmą paveikslėlį
                String photoReference = photoMetadata.zzb(); // gauti photo reference
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + itemView.getContext().getString(R.string.places_api_key);
                Glide.with(itemView)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image) // Placeholder image
                        .error(R.drawable.error_image) // Error image
                        .into(imageView);
                System.out.println("Image URL: " + imageUrl);
            } else {
                // Nėra photo metadata, rodyti placeholder image
                Glide.with(itemView)
                        .load(R.drawable.no_image_placeholder)
                        .into(imageView);
            }

            // Nustatyti pavadinimą
            nameTextView.setText(place.getName());
        }
        // Bind metodas vietai su atstumu nuo naudotojo vietos (SearchFragment)
        public void bind(PlaceWithDistance placeWithDistance) {
            Place place = placeWithDistance.getPlace();
            double distance = placeWithDistance.getDistance();

            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // Photo metadata yra
                PhotoMetadata photoMetadata = photoMetadataList.get(0); // // Gauti pirmą paveikslėlį
                String photoReference = photoMetadata.zzb(); // gauti photo reference
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + itemView.getContext().getString(R.string.places_api_key);
                Glide.with(itemView)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image) // Placeholder image
                        .error(R.drawable.error_image) // Error image
                        .into(imageView);
                System.out.println("Image URL: " + imageUrl);
            } else {
                // Nėra photo metadata, rodyti placeholder image
                Glide.with(itemView)
                        .load(R.drawable.no_image_placeholder) // Placeholder image
                        .into(imageView);
            }

            // Nustatyti pavadinimą ir atstumą
            nameTextView.setText(place.getName() + " - " + String.format("%.2f Kilometrai", distance/1000));

        }


    }

}
