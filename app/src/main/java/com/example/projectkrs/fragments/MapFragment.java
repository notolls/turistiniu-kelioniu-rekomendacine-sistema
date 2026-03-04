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
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng userLocation;
    private Spinner categorySpinner;
    private Button btnToggleVisited;

    private static final int DEFAULT_RADIUS = 10000;
    private String selectedCategory = "tourist_attraction";
    private String selectedMarkerDrawable = "marker_default";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Map<String, String> categoryTypesMap = new HashMap<>();

    private String viewUserId = null;
    private TileOverlay heatmapOverlay; // heatmap overlay
    private boolean heatmapVisible = false; // ar heatmap rodomas
    private List<WeightedLatLng> heatList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (getArguments() != null) {
            viewUserId = getArguments().getString("view_user_id");
            userLocation = getArguments().getParcelable("user_location");
        }

        categorySpinner = view.findViewById(R.id.categorySpinner);
        btnToggleVisited = view.findViewById(R.id.btnToggleVisited);

        if (viewUserId != null) {
            categorySpinner.setVisibility(View.GONE);
            btnToggleVisited.setVisibility(View.VISIBLE);
        } else {
            btnToggleVisited.setVisibility(View.GONE);
        }

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
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
                if (mMap != null && userLocation != null && viewUserId == null) {
                    fetchNearbyPlaces(userLocation);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnToggleVisited.setOnClickListener(v -> toggleHeatmap());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadUserSelectedMarker();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (viewUserId != null) {
            loadVisitedPlacesOfUser(viewUserId);
            return;
        }

        if (userLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));
            fetchNearbyPlaces(userLocation);
        }
    }

    // ===============================
    // KITO USER VISITED + HEATMAP
    // ===============================
    private void loadVisitedPlacesOfUser(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("places")
                .whereEqualTo("status", "visited")
                .get()
                .addOnSuccessListener(query -> {

                    if (!isAdded() || mMap == null) return;

                    mMap.clear();
                    heatList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String name = doc.getString("name");

                        if (lat == null || lng == null) continue;

                        LatLng position = new LatLng(lat, lng);

                        mMap.addMarker(new MarkerOptions()
                                .position(position)
                                .title(name));

                        heatList.add(new WeightedLatLng(position));
                    }

                    if (!query.isEmpty()) {
                        DocumentSnapshot first = query.getDocuments().get(0);
                        Double lat = first.getDouble("lat");
                        Double lng = first.getDouble("lng");
                        if (lat != null && lng != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lat, lng), 10f));
                        }
                    }
                });
    }

    private void toggleHeatmap() {
        if (heatmapOverlay != null) {
            heatmapOverlay.remove();
            heatmapOverlay = null;
            heatmapVisible = false;
        } else if (!heatList.isEmpty()) {
            HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                    .weightedData(heatList)
                    .radius(50)
                    .build();
            heatmapOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            heatmapVisible = true;
        }
    }

    // ===============================
    // SENAS FUNKCIONALUMAS
    // ===============================
    private void loadUserSelectedMarker() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("selectedMarker")) {
                        selectedMarkerDrawable = doc.getString("selectedMarker");
                        if (mMap != null && userLocation != null && viewUserId == null) {
                            fetchNearbyPlaces(userLocation);
                        }
                    }
                });
    }

    private Bitmap getBitmapFromVector(int drawableId) {
        try {
            if (getContext() == null) return null;
            final android.graphics.drawable.Drawable drawable =
                    ContextCompat.getDrawable(requireContext(), drawableId);
            if (drawable == null) return null;

            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e("MapFragment", "Bitmap conversion error", e);
            return null;
        }
    }

    private void fetchNearbyPlaces(LatLng location) {
        new Thread(() -> {
            try {
                String urlString =
                        "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                                "?location=" + location.latitude + "," + location.longitude +
                                "&radius=" + DEFAULT_RADIUS +
                                "&language=lt" +
                                "&type=" + selectedCategory +
                                "&key=" + getString(R.string.places_api_key);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray results = new JSONObject(response.toString()).getJSONArray("results");

                mainHandler.post(() -> {
                    if (!isAdded() || mMap == null) return;

                    mMap.clear();

                    int markerResId = getResources().getIdentifier(
                            selectedMarkerDrawable,
                            "drawable",
                            requireContext().getPackageName()
                    );

                    Bitmap markerBitmap = getBitmapFromVector(markerResId);

                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject obj = results.getJSONObject(i);
                            String name = obj.getString("name");
                            JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                            LatLng pos = new LatLng(
                                    loc.getDouble("lat"),
                                    loc.getDouble("lng")
                            );

                            MarkerOptions markerOptions =
                                    new MarkerOptions().position(pos).title(name);

                            if (markerBitmap != null) {
                                markerOptions.icon(
                                        BitmapDescriptorFactory.fromBitmap(markerBitmap)
                                );
                            }

                            mMap.addMarker(markerOptions);

                        } catch (Exception e) {
                            Log.e("MapFragment", "Marker error", e);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("MapFragment", "Fetch error", e);
            }
        }).start();
    }
}