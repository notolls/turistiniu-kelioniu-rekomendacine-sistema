package com.example.projectkrs.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.Intent;
import android.net.Uri;
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
import com.example.projectkrs.weather.WeatherOverlayView;
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
    private Button btnToggleVisited, btnWeather, btnAddToRoute, btnOpenRoute;
    private WeatherOverlayView weatherOverlay;

    private WeatherOverlayView.WeatherType currentWeather = WeatherOverlayView.WeatherType.NONE;
    private final WeatherOverlayView.WeatherType[] weatherTypes = {
            WeatherOverlayView.WeatherType.NONE,
            WeatherOverlayView.WeatherType.RAIN,
            WeatherOverlayView.WeatherType.SNOW,
            WeatherOverlayView.WeatherType.NIGHT,
            WeatherOverlayView.WeatherType.SUN
    };
    private int weatherIndex = 0;

    private static final int DEFAULT_RADIUS = 10000;
    private String selectedCategory = "tourist_attraction";
    private String selectedMarkerDrawable = "marker_default";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Map<String, String> categoryTypesMap = new HashMap<>();
    private String viewUserId = null;
    private TileOverlay heatmapOverlay;
    private boolean heatmapVisible = false;
    private final List<WeightedLatLng> heatList = new ArrayList<>();
    private final List<LatLng> routePoints = new ArrayList<>();
    private Marker selectedMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        categorySpinner = view.findViewById(R.id.categorySpinner);
        btnToggleVisited = view.findViewById(R.id.btnToggleVisited);
        btnWeather = view.findViewById(R.id.btnWeather);
        btnAddToRoute = view.findViewById(R.id.btnAddToRoute);
        btnOpenRoute = view.findViewById(R.id.btnOpenRoute);
        weatherOverlay = view.findViewById(R.id.weatherOverlay);

        if (getArguments() != null) {
            viewUserId = getArguments().getString("view_user_id");
            userLocation = getArguments().getParcelable("user_location");
        }

        if (viewUserId != null) {
            categorySpinner.setVisibility(View.GONE);
            btnToggleVisited.setVisibility(View.VISIBLE);
        } else {
            btnToggleVisited.setVisibility(View.GONE);
        }

        // Kategorijos
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Lankytinos vietos", "tourist_attraction");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>(categoryTypesMap.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
                if (mMap != null && userLocation != null && viewUserId == null) fetchNearbyPlaces(userLocation);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnToggleVisited.setOnClickListener(v -> toggleHeatmap());

        btnAddToRoute.setOnClickListener(v -> addSelectedMarkerToRoute());
        btnOpenRoute.setOnClickListener(v -> openRouteInGoogleMaps());

        // Weather button ciklas
        btnWeather.setOnClickListener(v -> {
            weatherIndex = (weatherIndex + 1) % weatherTypes.length;
            currentWeather = weatherTypes[weatherIndex];
            weatherOverlay.setWeather(currentWeather);
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        loadUserSelectedMarker();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;
            if (viewUserId == null) {
                btnAddToRoute.setVisibility(View.VISIBLE);
            }
            marker.showInfoWindow();
            return false;
        });

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
    // HEATMAP / VISITED PLACES
    // ===============================
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

    private void loadVisitedPlacesOfUser(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users").document(userId).collection("places")
                .whereEqualTo("status", "visited").get()
                .addOnSuccessListener(query -> {
                    if (!isAdded() || mMap == null) return;

                    mMap.clear();
                    resetRouteState();
                    heatList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String name = doc.getString("name");

                        if (lat == null || lng == null) continue;
                        LatLng pos = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions().position(pos).title(name));
                        heatList.add(new WeightedLatLng(pos));
                    }

                    if (!query.isEmpty()) {
                        DocumentSnapshot first = query.getDocuments().get(0);
                        Double lat = first.getDouble("lat");
                        Double lng = first.getDouble("lng");
                        if (lat != null && lng != null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10f));
                    }
                });
    }

    // ===============================
    // NEARBY PLACES + MARKERS
    // ===============================
    private void loadUserSelectedMarker() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("selectedMarker")) {
                        selectedMarkerDrawable = doc.getString("selectedMarker");
                        if (mMap != null && userLocation != null && viewUserId == null)
                            fetchNearbyPlaces(userLocation);
                    }
                });
    }

    private Bitmap getBitmapFromVector(int drawableId) {
        try {
            if (getContext() == null) return null;
            android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
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

    private void addSelectedMarkerToRoute() {
        if (selectedMarker == null) return;

        LatLng point = selectedMarker.getPosition();
        for (LatLng routePoint : routePoints) {
            if (routePoint.latitude == point.latitude && routePoint.longitude == point.longitude) {
                return;
            }
        }

        routePoints.add(point);
        btnOpenRoute.setVisibility(routePoints.size() >= 2 ? View.VISIBLE : View.GONE);
    }

    private void openRouteInGoogleMaps() {
        if (routePoints.size() < 2 || getContext() == null) return;

        LatLng origin = routePoints.get(0);
        LatLng destination = routePoints.get(routePoints.size() - 1);

        StringBuilder uriBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        uriBuilder.append("&origin=").append(origin.latitude).append(',').append(origin.longitude);
        uriBuilder.append("&destination=").append(destination.latitude).append(',').append(destination.longitude);
        uriBuilder.append("&travelmode=walking");

        if (routePoints.size() > 2) {
            uriBuilder.append("&waypoints=");
            for (int i = 1; i < routePoints.size() - 1; i++) {
                LatLng waypoint = routePoints.get(i);
                if (i > 1) uriBuilder.append('|');
                uriBuilder.append(waypoint.latitude).append(',').append(waypoint.longitude);
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriBuilder.toString()));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
            intent.setPackage(null);
        }

        startActivity(intent);
    }

    private void resetRouteState() {
        routePoints.clear();
        selectedMarker = null;
        if (btnAddToRoute != null) btnAddToRoute.setVisibility(View.GONE);
        if (btnOpenRoute != null) btnOpenRoute.setVisibility(View.GONE);
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

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONArray results = new JSONObject(response.toString()).getJSONArray("results");

                mainHandler.post(() -> {
                    if (!isAdded() || mMap == null) return;
                    mMap.clear();
                    resetRouteState();

                    int markerResId = getResources().getIdentifier(selectedMarkerDrawable, "drawable", requireContext().getPackageName());
                    Bitmap markerBitmap = getBitmapFromVector(markerResId);

                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject obj = results.getJSONObject(i);
                            String name = obj.getString("name");
                            JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                            LatLng pos = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                            MarkerOptions markerOptions = new MarkerOptions().position(pos).title(name);
                            if (markerBitmap != null)
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap));

                            mMap.addMarker(markerOptions);

                        } catch (Exception e) { Log.e("MapFragment", "Marker error", e); }
                    }
                });

            } catch (Exception e) { Log.e("MapFragment", "Fetch error", e); }
        }).start();
    }
}