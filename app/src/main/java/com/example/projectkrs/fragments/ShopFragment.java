package com.example.projectkrs.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectkrs.R;
import com.example.projectkrs.adapters.ShopAdapter;
import com.example.projectkrs.model.ShopMarker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShopAdapter adapter;
    private final List<ShopMarker> markers = new ArrayList<>();
    private TextView textPoints;

    private FirebaseFirestore db;
    private String userId;
    private int userPoints = 0;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.shopRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        textPoints = view.findViewById(R.id.textPoints);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(getContext(), "Neprisijungęs vartotojas", Toast.LENGTH_SHORT).show();
            return view;
        }

        adapter = new ShopAdapter(markers, this::onMarkerClicked);
        recyclerView.setAdapter(adapter);

        loadUserPoints();
        loadShopMarkers();

        return view;
    }

    // ===== Load user points from Firestore =====
    private void loadUserPoints() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("points")) {
                        userPoints = doc.getLong("points").intValue();
                    } else {
                        userPoints = 0;
                        db.collection("users").document(userId).update("points", 0);
                    }
                    textPoints.setText("Taškai: " + userPoints);
                })
                .addOnFailureListener(e -> textPoints.setText("Taškai: 0"));
    }

    // ===== Load shop markers from Firestore =====
    private void loadShopMarkers() {
        db.collection("shop_markers")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        // Jei kolekcija tuščia – sukurti default markerius
                        createDefaultMarkersForShop();
                        return;
                    }

                    markers.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ShopMarker marker = doc.toObject(ShopMarker.class);
                        if (marker != null) markers.add(marker);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Nepavyko užkrauti shop", Toast.LENGTH_SHORT).show());
    }

    private void createDefaultMarkersForShop() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> violetMarker = new HashMap<>();
        violetMarker.put("name", "Violetinis markeris");
        violetMarker.put("price", 10);
        violetMarker.put("drawable", "marker_violet");

        db.collection("shop_markers").add(violetMarker)
                .addOnSuccessListener(docRef -> loadShopMarkers()) // po sukūrimo iš karto pakartojam load
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Nepavyko sukurti default markerio", Toast.LENGTH_SHORT).show());
    }


    // ===== Buy / Select marker =====
    private void onMarkerClicked(ShopMarker marker) {
        if (userPoints >= marker.getPrice()) {
            userPoints -= marker.getPrice();
            textPoints.setText("Taškai: " + userPoints);

            db.collection("users").document(userId)
                    .update("points", userPoints,
                            "selectedMarker", marker.getDrawable())
                    .addOnSuccessListener(v -> Toast.makeText(
                            getContext(),
                            "Marker pasirinktas: " + marker.getName(),
                            Toast.LENGTH_SHORT
                    ).show())
                    .addOnFailureListener(e -> Toast.makeText(
                            getContext(),
                            "Nepavyko pasirinkti markerio",
                            Toast.LENGTH_SHORT
                    ).show());
        } else {
            Toast.makeText(
                    getContext(),
                    "Nepakanka taškų (" + userPoints + " / " + marker.getPrice() + ")",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
