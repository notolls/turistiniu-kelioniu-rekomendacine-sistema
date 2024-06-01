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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projectkrs.adapters.ImageSliderAdapter;
import com.example.projectkrs.dialogs.OptionsDialog;
import com.example.projectkrs.adapters.PostAdapter;
import com.example.projectkrs.R;
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

    private ViewPager2 viewPager;
    private RecyclerView postRecyclerView;
    private ImageSliderAdapter imageSliderAdapter;
    private PostAdapter postAdapter;
    private List<Place> placesList;

    private ImageView dot1, dot2, dot3;

    private final Handler slideHandler = new Handler();
    private Runnable slideRunnable;

    private LatLng userLocation;

    private Button optionsButton;
    private Map<String, Boolean> openingHoursMap = new HashMap<>();




    // Default atstumas metrais
    private static int DEFAULT_RADIUS = 10000;

    // Default kategoriją
    private static String DEFAULT_TYPE ="tourist_attraction";

    // konstruktorius
    public HomeFragment() {
    }


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

        optionsButton.setOnClickListener(v -> {
            showOptionsDialog(); // Pasirinkti atstumą ir kategoriją
        });


        // Gauti naudotojo lokaciją iš homeactivity
        userLocation = getArguments().getParcelable("user_location");

        placesList = new ArrayList<>();

        imageSliderAdapter = new ImageSliderAdapter(getContext(), placesList);
        viewPager.setAdapter(imageSliderAdapter);

        postAdapter = new PostAdapter(placesList);
        postAdapter.setOnItemClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postRecyclerView.setLayoutManager(layoutManager);
        postRecyclerView.setAdapter(postAdapter);

        fetchUserCategoryFromFirestore();



        return view;
    }

    private void fetchUserCategoryFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String selectedCategory = documentSnapshot.getString("categoryType");
                            if (!TextUtils.isEmpty(selectedCategory)) {
                                // Atnaujinti pasiulymus su naudotojo kategoriją pasirinkta registruojantis
                                DEFAULT_TYPE = selectedCategory;
                                Log.d("HomeFragment", "User selected category: " + selectedCategory);
                                // Fetch nearby places su naudotojo kategoriją pasirinkta registruojantis
                                fetchNearbyTouristAttractions(userLocation);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching user category type from Firestore", e));
        }
    }
    //metodas atidaryti dialogą
    public void showOptionsDialog() {
        OptionsDialog optionsDialog = new OptionsDialog(getContext(), this);
        optionsDialog.show();
    }

    public void handleOptionsDialogResult(int radius, String selectedCategory) {
        // atnaujinti atstumą ir kategoriją
        DEFAULT_RADIUS = radius*1000;
        DEFAULT_TYPE = selectedCategory;

        // Call fetchNearbyPlaces su naujomis reikšmėmis
        fetchNearbyTouristAttractions(userLocation);
    }



    private void fetchNearbyTouristAttractions(LatLng currentLocation) {
        Log.d("HomeFragment", "Fetching nearby places for location: " + currentLocation.latitude + ", " + currentLocation.longitude);

        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLocation.latitude + "," + currentLocation.longitude +
                        "&radius=" + DEFAULT_RADIUS +
                        "&language=lt"+
                        "&type=" + DEFAULT_TYPE +
                        "&key=" + getResources().getString(R.string.places_api_key); // Replace with your actual Places API key


                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Išskaidyti JSON atsaką
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");
                List<Place> places = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject placeObject = results.getJSONObject(i);

                    // Ištraukti place detales
                    String placeId = placeObject.getString("place_id");
                    String name = placeObject.getString("name");
                    String address = placeObject.getString("vicinity");
                    JSONObject geometryObject = placeObject.getJSONObject("geometry");
                    JSONObject locationObject = geometryObject.getJSONObject("location");
                    double latitude = locationObject.getDouble("lat");
                    double longitude = locationObject.getDouble("lng");

                    LatLng latLng = new LatLng(latitude, longitude);
                    List<PhotoMetadata> photoMetadataList = new ArrayList<>();

                    if (placeObject.has("photos")) {
                        JSONArray photosArray = placeObject.getJSONArray("photos");
                        for (int j = 0; j < photosArray.length(); j++) {
                            JSONObject photoObject = photosArray.getJSONObject(j);
                            String photoReference = photoObject.optString("photo_reference");

                            int width = photoObject.optInt("width");
                            int height = photoObject.optInt("height");
                            JSONArray htmlAttributionsArray = photoObject.optJSONArray("html_attributions");
                            List<String> htmlAttributions = new ArrayList<>();
                            if (htmlAttributionsArray != null) {
                                for (int k = 0; k < htmlAttributionsArray.length(); k++) {
                                    htmlAttributions.add(htmlAttributionsArray.getString(k));
                                }
                            }

                            // Sukurti PhotoMetadata objektą naudotojant Builder
                            PhotoMetadata photoMetadata = PhotoMetadata.builder(photoReference)
                                    .setWidth(width)
                                    .setHeight(height)
                                    .build();
                            photoMetadataList.add(photoMetadata);
                        }
                    }

                    boolean isOpenNow = false;
                    if (placeObject.has("opening_hours")) {
                        JSONObject openingHoursObject = placeObject.getJSONObject("opening_hours");
                        isOpenNow = openingHoursObject.getBoolean("open_now");
                    }

                    // Saugoti darbo laiko informaciją map'e
                    openingHoursMap.put(placeId, isOpenNow);


                    // sukurti Place object su photo metadata
                    Place place = Place.builder()
                            .setId(placeId)
                            .setName(name)
                            .setAddress(address)
                            .setPhotoMetadatas(photoMetadataList)
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
        // Atnaujiti UI su gautomis vietomis
        placesList.clear();
        placesList.addAll(places);

        // Prideti 3 vietas į ViewPager
        List<Place> viewPagerPlaces = new ArrayList<>();
        if (places.size() >= 3) {
            viewPagerPlaces.addAll(places.subList(0, 3)); // pirmos 3 vietos
        } else {
            viewPagerPlaces.addAll(places); // idėti visas jei mažiau nei 3
        }

        // Atnaujinti ViewPager adapter su naujomis vietomis
        imageSliderAdapter.updateData(viewPagerPlaces);
        imageSliderAdapter.notifyDataSetChanged();

        // Atnaujinti PostAdapter su visomis vietomis
        postAdapter.updateData(places);
        postAdapter.notifyDataSetChanged();

        updateDots(0); // Update UI elements like dots
        startAutoSlide(); // Start auto sliding
    }

    private void updateDots(int position) {
        switch (position) {
            case 0:
                dot1.setImageResource(R.drawable.dot_selected);
                dot2.setImageResource(R.drawable.dot_unselected);
                dot3.setImageResource(R.drawable.dot_unselected);
                break;
            case 1:
                dot1.setImageResource(R.drawable.dot_unselected);
                dot2.setImageResource(R.drawable.dot_selected);
                dot3.setImageResource(R.drawable.dot_unselected);
                break;
            case 2:
                dot1.setImageResource(R.drawable.dot_unselected);
                dot2.setImageResource(R.drawable.dot_unselected);
                dot3.setImageResource(R.drawable.dot_selected);
                break;
        }
    }

    private void startAutoSlide() {
        slideHandler.removeCallbacks(slideRunnable);
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                int nextSlide = viewPager.getCurrentItem() + 1;
                if (nextSlide >= 3) {
                    nextSlide = 0;
                }
                viewPager.setCurrentItem(nextSlide, true);
                updateDots(nextSlide);
                slideHandler.postDelayed(this, SLIDE_INTERVAL);
            }
        };
        slideHandler.postDelayed(slideRunnable, SLIDE_INTERVAL);
    }

    private boolean getOpeningHours(String placeId) {
        return Boolean.TRUE.equals(openingHoursMap.getOrDefault(placeId, false));
    }


    @Override
    public void onItemClick(int position) {
        Place selectedPlace = placesList.get(position);
        boolean isOpenNow = getOpeningHours(selectedPlace.getId());
        openDetailActivity(selectedPlace, isOpenNow);
    }
    private void openDetailActivity(Place place, boolean isOpenNow) {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra("place", place); // Perduoti Place object
        intent.putExtra("isOpenNow", isOpenNow); // Perduoti darbo laiko informaciją
        startActivity(intent);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        slideHandler.removeCallbacks(slideRunnable);
    }
}