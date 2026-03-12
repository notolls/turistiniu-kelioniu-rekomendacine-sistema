package com.example.projectkrs.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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

public class MapFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private LatLng userLocation;
    private Spinner categorySpinner;
    private Button btnToggleVisited, btnWeather, btnAddToRoute, btnOpenRoute, btnToggleCompass;
    private ImageView ivCompass;
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
    private final Map<String, Bitmap> photoCache = new HashMap<>();

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Sensor accelerometerSensor;
    private Sensor magneticFieldSensor;
    private boolean compassEnabled = true;
    private float currentAzimuth = 0f;
    private final float[] gravityValues = new float[3];
    private final float[] magneticValues = new float[3];
    private boolean hasGravityValues = false;
    private boolean hasMagneticValues = false;

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
        btnToggleCompass = view.findViewById(R.id.btnToggleCompass);
        ivCompass = view.findViewById(R.id.ivCompass);
        weatherOverlay = view.findViewById(R.id.weatherOverlay);

        sensorManager = (SensorManager) requireContext().getSystemService(android.content.Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

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
        btnToggleCompass.setOnClickListener(v -> toggleCompass());

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
        updateCompassUi();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerCompassSensors();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        setupInfoWindowAdapter();
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

    private void setupInfoWindowAdapter() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                View infoView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_map_info_window, null);

                TextView tvTitle = infoView.findViewById(R.id.tvInfoTitle);
                TextView tvAddress = infoView.findViewById(R.id.tvInfoAddress);
                ImageView ivPhoto = infoView.findViewById(R.id.ivInfoPhoto);

                tvTitle.setText(marker.getTitle());

                PlaceInfo placeInfo = marker.getTag() instanceof PlaceInfo
                        ? (PlaceInfo) marker.getTag()
                        : null;

                if (placeInfo == null) {
                    tvAddress.setVisibility(View.GONE);
                    ivPhoto.setVisibility(View.GONE);
                    return infoView;
                }

                if (placeInfo.address == null || placeInfo.address.isEmpty()) {
                    tvAddress.setVisibility(View.GONE);
                } else {
                    tvAddress.setVisibility(View.VISIBLE);
                    tvAddress.setText(placeInfo.address);
                }

                if (placeInfo.photoUrl == null || placeInfo.photoUrl.isEmpty()) {
                    ivPhoto.setVisibility(View.GONE);
                    return infoView;
                }

                Bitmap cached = photoCache.get(placeInfo.photoUrl);
                if (cached != null) {
                    ivPhoto.setVisibility(View.VISIBLE);
                    ivPhoto.setImageBitmap(cached);
                    return infoView;
                }

                ivPhoto.setVisibility(View.GONE);
                loadInfoWindowPhoto(marker, placeInfo.photoUrl);
                return infoView;
            }
        });
    }

    private void toggleCompass() {
        compassEnabled = !compassEnabled;
        if (compassEnabled) {
            registerCompassSensors();
        } else if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        updateCompassUi();
    }

    private void updateCompassUi() {
        if (ivCompass != null) {
            ivCompass.setVisibility(compassEnabled ? View.VISIBLE : View.GONE);
        }
        if (btnToggleCompass != null) {
            btnToggleCompass.setText(compassEnabled
                    ? getString(R.string.compass_disable)
                    : getString(R.string.compass_enable));
        }
    }

    private void registerCompassSensors() {
        if (!compassEnabled || sensorManager == null) return;

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
            return;
        }

        if (accelerometerSensor != null && magneticFieldSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!compassEnabled || ivCompass == null) return;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationValues);
            updateCompassRotation((float) Math.toDegrees(orientationValues[0]));
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, gravityValues.length);
            hasGravityValues = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, magneticValues.length);
            hasMagneticValues = true;
        }

        if (hasGravityValues && hasMagneticValues) {
            float[] rotationMatrix = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)) {
                float[] orientationValues = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationValues);
                updateCompassRotation((float) Math.toDegrees(orientationValues[0]));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nereikia papildomos logikos.
    }

    private void updateCompassRotation(float azimuthDegrees) {
        float normalizedAzimuth = (azimuthDegrees + 360f) % 360f;
        ivCompass.animate().rotation(-normalizedAzimuth).setDuration(120).start();
        currentAzimuth = normalizedAzimuth;
    }

    private void loadInfoWindowPhoto(Marker marker, String photoUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(photoUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                if (bitmap != null) {
                    photoCache.put(photoUrl, bitmap);
                    mainHandler.post(() -> {
                        if (marker.isInfoWindowShown()) {
                            marker.showInfoWindow();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("MapFragment", "Info window photo load error", e);
            }
        }).start();
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
            if (isSamePoint(routePoint, point)) {
                return;
            }
        }

        routePoints.add(point);
        btnOpenRoute.setVisibility(canOpenRoute(routePoints.size()) ? View.VISIBLE : View.GONE);
    }

    private void openRouteInGoogleMaps() {
        if (!canOpenRoute(routePoints.size()) || getContext() == null) return;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(buildGoogleMapsRouteUrl(routePoints)));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
            intent.setPackage(null);
        }

        startActivity(intent);
    }


    static boolean isSamePoint(LatLng first, LatLng second) {
        return first != null && second != null
                && first.latitude == second.latitude
                && first.longitude == second.longitude;
    }

    static boolean canOpenRoute(int routePointCount) {
        return routePointCount >= 2;
    }

    static String buildGoogleMapsRouteUrl(List<LatLng> routePoints) {
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

        return uriBuilder.toString();
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
                            String address = obj.optString("vicinity", "");
                            JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                            LatLng pos = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));

                            String photoUrl = "";
                            JSONArray photos = obj.optJSONArray("photos");
                            if (photos != null && photos.length() > 0) {
                                String photoReference = photos.getJSONObject(0).optString("photo_reference", "");
                                if (!photoReference.isEmpty()) {
                                    photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                                            "?maxwidth=400" +
                                            "&photo_reference=" + photoReference +
                                            "&key=" + getString(R.string.places_api_key);
                                }
                            }

                            MarkerOptions markerOptions = new MarkerOptions().position(pos).title(name);
                            if (markerBitmap != null)
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap));

                            Marker marker = mMap.addMarker(markerOptions);
                            if (marker != null) {
                                marker.setTag(new PlaceInfo(address, photoUrl));
                            }

                        } catch (Exception e) { Log.e("MapFragment", "Marker error", e); }
                    }
                });

            } catch (Exception e) { Log.e("MapFragment", "Fetch error", e); }
        }).start();
    }

    private static class PlaceInfo {
        final String address;
        final String photoUrl;

        PlaceInfo(String address, String photoUrl) {
            this.address = address;
            this.photoUrl = photoUrl;
        }
    }
}
