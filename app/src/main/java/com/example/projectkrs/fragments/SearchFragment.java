package com.example.projectkrs.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectkrs.adapters.PostAdapter;
import com.example.projectkrs.model.PlaceWithDistance;
import com.example.projectkrs.R;
import com.example.projectkrs.activities.PostDetailActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment implements PostAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PostAdapter searchAdapter;
    private List<PlaceWithDistance> searchResults;
    private List<PlaceWithDistance> allUserPlaces;
    private LatLng userLocation;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private MaterialButton buttonAll, buttonWant, buttonVisited;
    private String currentFilter = "all"; // "all", "want", "visited"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userLocation = getArguments() != null ? getArguments().getParcelable("user_location") : null;

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults = new ArrayList<>();
        allUserPlaces = new ArrayList<>();
        searchAdapter = new PostAdapter(searchResults);
        searchAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(searchAdapter);

        // Filter buttons
        buttonAll = view.findViewById(R.id.buttonAll);
        buttonWant = view.findViewById(R.id.buttonWant);
        buttonVisited = view.findViewById(R.id.buttonVisited);

        buttonAll.setOnClickListener(v -> {
            currentFilter = "all";
            applyFilter();
            highlightSelectedButton(buttonAll);
        });

        buttonWant.setOnClickListener(v -> {
            currentFilter = "want";
            applyFilter();
            highlightSelectedButton(buttonWant);
        });

        buttonVisited.setOnClickListener(v -> {
            currentFilter = "visited";
            applyFilter();
            highlightSelectedButton(buttonVisited);
        });

        highlightSelectedButton(buttonAll); // initial highlight

        // Places API initialization
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.places_api_key));
        }

        // SearchView setup
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchResults.clear();
                searchAdapter.notifyDataSetChanged();
                return true;
            }
        });

        loadUserPlacesFromFirestore();

        return view;
    }

    private void highlightSelectedButton(MaterialButton selectedButton) {
        // Reset all buttons
        buttonAll.setBackgroundColor(getResources().getColor(R.color.white));
        buttonWant.setBackgroundColor(getResources().getColor(R.color.white));
        buttonVisited.setBackgroundColor(getResources().getColor(R.color.white));

        // Highlight selected button
        selectedButton.setBackgroundColor(getResources().getColor(R.color.purple_200));
    }

    private void loadUserPlacesFromFirestore() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("places")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            Double lat = doc.getDouble("lat");
                            Double lng = doc.getDouble("lng");
                            String name = doc.getString("name");
                            String address = doc.getString("address");
                            List<String> photoReferences = (List<String>) doc.get("photoReferences");

                            if (lat != null && lng != null) {
                                Place.Builder builder = Place.builder()
                                        .setName(name)
                                        .setAddress(address)
                                        .setLatLng(new LatLng(lat, lng))
                                        .setId(doc.getId());

                                if (photoReferences != null && !photoReferences.isEmpty()) {
                                    List<PhotoMetadata> photoMetadataList = new ArrayList<>();
                                    for (String ref : photoReferences) {
                                        photoMetadataList.add(PhotoMetadata.builder(ref).build());
                                    }
                                    builder.setPhotoMetadatas(photoMetadataList);
                                }

                                Place place = builder.build();
                                double distance = SphericalUtil.computeDistanceBetween(userLocation, place.getLatLng());
                                PlaceWithDistance pwd = new PlaceWithDistance(place, distance);
                                pwd.setStatus(status);
                                allUserPlaces.add(pwd);
                            }
                        }

                        applyFilter();
                    } else {
                        Log.e("SearchFragment", "Error fetching user places", task.getException());
                    }
                });
    }

    private void applyFilter() {
        searchResults.clear();
        for (PlaceWithDistance pwd : allUserPlaces) {
            if ("all".equals(currentFilter) || pwd.getStatus().equals(currentFilter)) {
                searchResults.add(pwd);
            }
        }
        Collections.sort(searchResults, Comparator.comparingDouble(PlaceWithDistance::getDistance));
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        PlaceWithDistance clickedPlace = searchResults.get(position);
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra("place", clickedPlace.getPlace());
        startActivity(intent);
    }

    private void performSearch(String query) {
        PlacesClient placesClient = Places.createClient(requireContext());
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setLocationBias(RectangularBounds.newInstance(LatLngBounds.builder().include(userLocation).build()))
                .build();

        placesClient.findAutocompletePredictions(request).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction :
                        Objects.requireNonNull(task.getResult()).getAutocompletePredictions()) {
                    fetchPlaceDetails(prediction.getPlaceId());
                }
            }
        });
    }

    private void fetchPlaceDetails(String placeId) {
        PlacesClient placesClient = Places.createClient(requireContext());
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS
        );
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        placesClient.fetchPlace(request).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Place place = task.getResult().getPlace();
                double distance = SphericalUtil.computeDistanceBetween(userLocation, place.getLatLng());
                PlaceWithDistance pwd = new PlaceWithDistance(place, distance);
                pwd.setStatus("all"); // naujos vietos status default "all"
                searchResults.add(pwd);
                Collections.sort(searchResults, Comparator.comparingDouble(PlaceWithDistance::getDistance));
                searchAdapter.notifyDataSetChanged();
            }
        });
    }
}
