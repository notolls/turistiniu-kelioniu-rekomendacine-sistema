package com.example.projectkrs.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projectkrs.R;
import com.example.projectkrs.adapters.ImageSliderAdapter;
import com.example.projectkrs.adapters.PostAdapter;
import com.example.projectkrs.dialogs.OptionsDialog;
import com.example.projectkrs.activities.PostDetailActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements PostAdapter.OnItemClickListener {

    private static final long SLIDE_INTERVAL = 5000;
    private static int DEFAULT_RADIUS = 10000;
    private static String DEFAULT_TYPE ="tourist_attraction";

    private ViewPager2 viewPager;
    private RecyclerView postRecyclerView;
    private ImageSliderAdapter imageSliderAdapter;
    private PostAdapter postAdapter;
    private List<Place> placesList;
    private ImageView dot1, dot2, dot3;
    private final Handler slideHandler = new Handler();
    private Runnable slideRunnable;
    private LatLng userLocation;
    private Map<String, Boolean> openingHoursMap = new HashMap<>();

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
        postRecyclerView = view.findViewById(R.id.postRecyclerView);

        Button optionsButton = view.findViewById(R.id.optionsButton);
        ImageView statisticsIcon = view.findViewById(R.id.buttonStatisticsIcon);

        optionsButton.setOnClickListener(v -> showOptionsDialog());
        statisticsIcon.setOnClickListener(v -> openStatisticsFragment());

        // Naudotojo lokacija iš HomeActivity
        userLocation = getArguments().getParcelable("user_location");

        placesList = new ArrayList<>();
        imageSliderAdapter = new ImageSliderAdapter(getContext(), placesList);
        viewPager.setAdapter(imageSliderAdapter);

        postAdapter = new PostAdapter(placesList);
        postAdapter.setOnItemClickListener(this);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postRecyclerView.setAdapter(postAdapter);

        fetchUserCategoryFromFirestore();

        return view;
    }

    private void openStatisticsFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new com.example.projectkrs.fragments.StatisticsFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchUserCategoryFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String selectedCategory = documentSnapshot.getString("categoryType");
                            if (!TextUtils.isEmpty(selectedCategory)) {
                                DEFAULT_TYPE = selectedCategory;
                                fetchNearbyTouristAttractions(userLocation);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching user category type", e));
        }
    }

    public void showOptionsDialog() {
        OptionsDialog optionsDialog = new OptionsDialog(getContext(), this);
        optionsDialog.show();
    }

    public void handleOptionsDialogResult(int radius, String selectedCategory) {
        DEFAULT_RADIUS = radius * 1000;
        DEFAULT_TYPE = selectedCategory;
        fetchNearbyTouristAttractions(userLocation);
    }

    private void fetchNearbyTouristAttractions(LatLng currentLocation) {
        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLocation.latitude + "," + currentLocation.longitude +
                        "&radius=" + DEFAULT_RADIUS +
                        "&language=lt"+
                        "&type=" + DEFAULT_TYPE +
                        "&key=" + getResources().getString(R.string.places_api_key);

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");
                List<Place> places = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject placeObject = results.getJSONObject(i);
                    String placeId = placeObject.getString("place_id");
                    String name = placeObject.getString("name");
                    String address = placeObject.getString("vicinity");

                    JSONObject geometryObject = placeObject.getJSONObject("geometry");
                    JSONObject locationObject = geometryObject.getJSONObject("location");
                    LatLng latLng = new LatLng(locationObject.getDouble("lat"), locationObject.getDouble("lng"));

                    List<PhotoMetadata> photos = new ArrayList<>();
                    if (placeObject.has("photos")) {
                        JSONArray photosArray = placeObject.getJSONArray("photos");
                        for (int j = 0; j < photosArray.length(); j++) {
                            JSONObject photoObject = photosArray.getJSONObject(j);
                            photos.add(PhotoMetadata.builder(photoObject.optString("photo_reference"))
                                    .setWidth(photoObject.optInt("width"))
                                    .setHeight(photoObject.optInt("height"))
                                    .build());
                        }
                    }

                    boolean isOpenNow = false;
                    if (placeObject.has("opening_hours")) {
                        isOpenNow = placeObject.getJSONObject("opening_hours").optBoolean("open_now", false);
                    }
                    openingHoursMap.put(placeId, isOpenNow);

                    Place place = Place.builder()
                            .setId(placeId)
                            .setName(name)
                            .setAddress(address)
                            .setPhotoMetadatas(photos)
                            .setLatLng(latLng)
                            .build();
                    places.add(place);
                }

                requireActivity().runOnUiThread(() -> onPlacesFetched(places));

            } catch (IOException | JSONException e) {
                Log.e("HomeFragment", "Error fetching nearby places", e);
            }
        }).start();
    }

    private void onPlacesFetched(List<Place> places) {
        placesList.clear();
        placesList.addAll(places);

        // Top 3 vietos į ViewPager
        List<Place> top3 = new ArrayList<>();
        for (int i = 0; i < Math.min(3, places.size()); i++) top3.add(places.get(i));
        imageSliderAdapter.updateData(top3);
        imageSliderAdapter.notifyDataSetChanged();

        postAdapter.updateData(places); // RecyclerView rodo viską
        postAdapter.notifyDataSetChanged();

        updateDots(0);
        startAutoSlide();
    }

    private void updateDots(int position) {
        dot1.setImageResource(position == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
        dot2.setImageResource(position == 1 ? R.drawable.dot_selected : R.drawable.dot_unselected);
        dot3.setImageResource(position == 2 ? R.drawable.dot_selected : R.drawable.dot_unselected);
    }

    private void startAutoSlide() {
        slideHandler.removeCallbacks(slideRunnable);
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                int next = (viewPager.getCurrentItem() + 1) % 3;
                viewPager.setCurrentItem(next, true);
                updateDots(next);
                slideHandler.postDelayed(this, SLIDE_INTERVAL);
            }
        };
        slideHandler.postDelayed(slideRunnable, SLIDE_INTERVAL);
    }

    @Override
    public void onItemClick(int position) {
        Place selectedPlace = placesList.get(position);
        boolean isOpenNow = openingHoursMap.getOrDefault(selectedPlace.getId(), false);
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra("place", selectedPlace);
        intent.putExtra("isOpenNow", isOpenNow);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        slideHandler.removeCallbacks(slideRunnable);
    }
}
