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
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng userLocation;

    private Button buttonRefresh;
    private Spinner categorySpinner;

    private static final int DEFAULT_RADIUS = 10000;
    private String selectedCategory = "tourist_attraction";
    private String selectedMarkerDrawable = "marker_default"; // default marker

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Map<String, String> categoryTypesMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Google Maps fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonRefresh = view.findViewById(R.id.buttonRefresh);
        categorySpinner = view.findViewById(R.id.categorySpinner);

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

        // Gaunam user location iš arguments
        if (getArguments() != null) {
            userLocation = getArguments().getParcelable("user_location");
        }

        // Nuskaitom vartotojo pasirinktą markerį
        loadUserSelectedMarker();

        buttonRefresh.setOnClickListener(v -> {
            if (mMap == null || userLocation == null) return;
            selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
            fetchNearbyPlaces(userLocation);
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (userLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f));
            fetchNearbyPlaces(userLocation);
        }
    }

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
                    }
                });
    }

    /**
     * Konvertuoja vektorinę drawable į Bitmap
     */
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

                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.getJSONArray("results");

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

                            JSONObject loc = obj
                                    .getJSONObject("geometry")
                                    .getJSONObject("location");

                            LatLng pos = new LatLng(
                                    loc.getDouble("lat"),
                                    loc.getDouble("lng")
                            );

                            if (markerBitmap != null) {
                                mMap.addMarker(
                                        new MarkerOptions()
                                                .position(pos)
                                                .title(name)
                                                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                                );
                            } else {
                                mMap.addMarker(
                                        new MarkerOptions()
                                                .position(pos)
                                                .title(name)
                                );
                            }

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
