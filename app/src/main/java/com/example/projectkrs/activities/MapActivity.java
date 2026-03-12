package com.example.projectkrs.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectkrs.R;
import com.example.projectkrs.fragments.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends AppCompatActivity {

    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Gauti naudotojo buvimo vietą iš pagrindinės veiklos
        userLocation = extractUserLocation(getIntent() == null ? null : getIntent().getExtras());

        // Sukurti naują „MapFragment“ instanciją.
        MapFragment mapFragment = new MapFragment();

        // Nustatyti fragmento parametrus
        Bundle bundle = new Bundle();
        if (shouldAttachUserLocation(userLocation)) {
            bundle.putParcelable("user_location", userLocation);
        }
        mapFragment.setArguments(bundle);

        // Pakeisti rodomą fragmentą
        replaceFragment(mapFragment);
    }


    static boolean shouldAttachUserLocation(LatLng userLocation) {
        return userLocation != null;
    }

    static LatLng extractUserLocation(Bundle extras) {
        if (extras == null) {
            return null;
        }
        return extras.getParcelable("user_location");
    }

    // Fragmentų pakeitimo metodas
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}