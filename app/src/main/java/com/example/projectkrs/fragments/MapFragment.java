package com.example.projectkrs.fragments;

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
import androidx.fragment.app.Fragment;

import com.example.projectkrs.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
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
    private ArrayAdapter<String> categoryAdapter;
    private Map<String, String> categoryTypesMap;

    private static final int DEFAULT_RADIUS = 10000;
    private String selectedCategory = "tourist_attraction";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonRefresh = view.findViewById(R.id.buttonRefresh);
        categorySpinner = view.findViewById(R.id.categorySpinner);

        categoryTypesMap = new HashMap<>();
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Autobusų stotys", "bus_station");
        categoryTypesMap.put("Traukinių stotys", "train_station");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Prekybos centrai", "shopping_mall");
        categoryTypesMap.put("Bendros lankytinos vietos", "tourist_attraction");

        categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(categoryTypesMap.keySet())
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        if (getArguments() != null) {
            userLocation = getArguments().getParcelable("user_location");
        }

        buttonRefresh.setOnClickListener(v -> {
            if (mMap == null || !isAdded()) return;

            selectedCategory = categoryTypesMap.get(
                    categorySpinner.getSelectedItem().toString()
            );

            fetchNearbyPlaces(mMap.getCameraPosition().target);
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
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                mainHandler.post(() -> {
                    if (!isAdded() || mMap == null) return;

                    mMap.clear();

                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject place = results.getJSONObject(i);
                            String name = place.getString("name");

                            JSONObject loc = place
                                    .getJSONObject("geometry")
                                    .getJSONObject("location");

                            LatLng pos = new LatLng(
                                    loc.getDouble("lat"),
                                    loc.getDouble("lng")
                            );

                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(pos)
                                            .title(name)
                            );
                        } catch (JSONException e) {
                            Log.e("MapFragment", "JSON parse error", e);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("MapFragment", "Error fetching places", e);
            }
        }).start();
    }
}
