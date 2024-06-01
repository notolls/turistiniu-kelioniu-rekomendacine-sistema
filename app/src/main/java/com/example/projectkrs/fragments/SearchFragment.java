package com.example.projectkrs.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectkrs.model.PlaceWithDistance;
import com.example.projectkrs.adapters.PostAdapter;
import com.example.projectkrs.R;
import com.example.projectkrs.activities.PostDetailActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;

import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;

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

    private LatLng userLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Gauti user location iš HomeActivity
        userLocation = getArguments().getParcelable("user_location");

        recyclerView = view.findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults = new ArrayList<>();
        searchAdapter = new PostAdapter(searchResults);
        searchAdapter.setOnItemClickListener(this);

        recyclerView.setAdapter(searchAdapter);

        // gauti API raktą
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.places_api_key));
        }

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // atlikti paiešką
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Išvalyti rezultatus pakeitus tekstą
                searchResults.clear();
                searchAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return view;
    }

    // Paspausti pasirinktą vietą
    @Override
    public void onItemClick(int position) {
        PlaceWithDistance clickedPlace = searchResults.get(position);
        openPostDetailActivity(clickedPlace.getPlace());
    }

    //atidaryti pasirinktą vietą
    private void openPostDetailActivity(Place place) {
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra("place", place);
        startActivity(intent);
    }

    private void performSearch(String query) {
        // Naudoti „Places API“, kad atliktume tekstinę paiešką pagal naudotojo užklausą
        PlacesClient placesClient = Places.createClient(requireContext());

        // Sukurti naują užklausą automatinio užbaigimo prognozėms su vieta
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setLocationBias(RectangularBounds.newInstance(
                        LatLngBounds.builder().include(userLocation).build()
                ))
                .build();

        // Asinchroninis prognozių gavimas
        placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                if (task.isSuccessful()) {
                    // Apdoroti prognozes ir gauti informaciją apie kiekvieną vietą
                    for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction : Objects.requireNonNull(task.getResult()).getAutocompletePredictions()) {
                        // Kiekvienos vietos informacijos gavimas
                        fetchPlaceDetails(prediction.getPlaceId(), prediction.getFullText(null).toString());
                    }
                } else {
                    // errorai
                }
            }
        });
    }

    private void fetchPlaceDetails(String placeId, String fullText) {
        PlacesClient placesClient = Places.createClient(requireContext());

        // Nurodyti laukus, kuriuos reikia gauti
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // sukurti FetchPlaceRequest
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        // Asinchroniškai gauti informaciją apie vietą
        placesClient.fetchPlace(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Place place = task.getResult().getPlace();
                double distance = SphericalUtil.computeDistanceBetween(userLocation, place.getLatLng());
                PlaceWithDistance placeWithDistance = new PlaceWithDistance(place, distance);
                searchResults.add(placeWithDistance);
                Collections.sort(searchResults, new Comparator<PlaceWithDistance>() {
                    @Override
                    public int compare(PlaceWithDistance p1, PlaceWithDistance p2) {
                        return Double.compare(p1.getDistance(), p2.getDistance());
                    }
                });
                searchAdapter.notifyDataSetChanged();
            } else {
                // Errorai
            }
        });
    }
}
