package com.example.projectkrs.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.projectkrs.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng userLocation;
    private Spinner categorySpinner;
    private Button btnToggleVisited;

    private boolean showHeatmap = false;
    private String selectedCategory = "tourist_attraction";
    private String selectedMarkerDrawable = "marker_default";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Map<String, String> categoryTypesMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        categorySpinner = view.findViewById(R.id.categorySpinner);
        btnToggleVisited = view.findViewById(R.id.btnToggleVisited);

        // Kategorijos
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Lankytinos vietos", "tourist_attraction");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(categoryTypesMap.keySet())
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
                if (mMap != null) fetchNearbyPlaces();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnToggleVisited.setOnClickListener(v -> {
            showHeatmap = !showHeatmap;
            btnToggleVisited.setText(showHeatmap ? "Rodyti markerius" : "Rodyti heatmap");
            if (mMap != null) fetchNearbyPlaces();
        });

        // Google Maps fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // vietos koordinatės
        if (getArguments() != null) {
            userLocation = getArguments().getParcelable("user_location");
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (userLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));
            fetchNearbyPlaces();
        }
    }

    private void fetchNearbyPlaces() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || mMap == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("places")
                .get()
                .addOnSuccessListener((QuerySnapshot querySnapshot) -> {

                    List<LatLng> heatmapPoints = new ArrayList<>();
                    mMap.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");
                        Double latObj = doc.getDouble("lat");
                        Double lngObj = doc.getDouble("lng");
                        Long visitsObj = doc.getLong("visits");

                        if (latObj == null || lngObj == null) continue;

                        LatLng pos = new LatLng(latObj, lngObj);
                        int frequency = visitsObj != null ? visitsObj.intValue() : 1;

                        if (showHeatmap) {
                            if ("visited".equals(status)) {
                                for (int i = 0; i < frequency; i++) heatmapPoints.add(pos);
                            }
                        } else {
                            if ("visited".equals(status) || "want".equals(status)) {
                                int markerResId = getResources().getIdentifier(
                                        selectedMarkerDrawable, "drawable", requireContext().getPackageName());
                                Bitmap bitmap = getBitmapFromVector(markerResId);

                                MarkerOptions markerOptions = new MarkerOptions().position(pos).title(doc.getString("name"));
                                if (bitmap != null) markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                mMap.addMarker(markerOptions);
                            }
                        }
                    }

                    if (showHeatmap && !heatmapPoints.isEmpty()) {
                        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                                .data(heatmapPoints)
                                .radius(50)
                                .opacity(0.7)
                                .build();
                        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
                    }
                })
                .addOnFailureListener(e -> Log.e("MapFragment", "Failed to fetch places", e));
    }

    private Bitmap getBitmapFromVector(int drawableId) {
        try {
            if (getContext() == null) return null;
            final android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
            if (drawable == null) return null;

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e("MapFragment", "Bitmap conversion error", e);
            return null;
        }
    }
}