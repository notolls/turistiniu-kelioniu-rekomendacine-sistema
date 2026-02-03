package com.example.projectkrs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectkrs.R;
import com.example.projectkrs.model.ShopMarker;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    public interface OnMarkerClickListener {
        void onMarkerClick(ShopMarker marker);
    }

    private final List<ShopMarker> markers;
    private final OnMarkerClickListener listener;

    public ShopAdapter(List<ShopMarker> markers, OnMarkerClickListener listener) {
        this.markers = markers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_marker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopMarker marker = markers.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(marker.getName());
        holder.price.setText(marker.getPrice() + " pts");

        int resId = context.getResources().getIdentifier(
                marker.getDrawable(),
                "drawable",
                context.getPackageName()
        );
        holder.icon.setImageResource(resId);

        holder.button.setOnClickListener(v -> {
            if (listener != null) listener.onMarkerClick(marker);
        });
    }

    @Override
    public int getItemCount() { return markers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, price;
        Button button;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.markerIcon);
            name = itemView.findViewById(R.id.markerName);
            price = itemView.findViewById(R.id.markerPrice);
            button = itemView.findViewById(R.id.markerActionButton);
        }
    }
}
