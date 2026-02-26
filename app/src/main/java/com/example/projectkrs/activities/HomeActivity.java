package com.example.projectkrs.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectkrs.R;
import com.example.projectkrs.fragments.HomeFragment;
import com.example.projectkrs.fragments.MapFragment;
import com.example.projectkrs.fragments.ProfileFragment;
import com.example.projectkrs.fragments.SearchFragment;
import com.example.projectkrs.fragments.ShopFragment;
import com.example.projectkrs.fragments.StatisticsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        userLocation = getIntent().getParcelableExtra("user_location");

        // Vienkartinis default shop_markers įkėlimas
        initDefaultShopMarkers();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            Bundle args = new Bundle();
            args.putParcelable("user_location", userLocation);

            switch (item.getItemId()) {
                case R.id.navigation_home: fragment = new HomeFragment(); break;
                case R.id.navigation_search: fragment = new SearchFragment(); break;
                case R.id.navigation_map: fragment = new MapFragment(); break;
                case R.id.navigation_shop: fragment = new ShopFragment(); break;
                case R.id.navigation_profile: fragment = new ProfileFragment();
//                case R.id.navigation_statistics: fragment = new StatisticsFragment();
            }

            if (fragment != null) {
                fragment.setArguments(args);
                replaceFragment(fragment);
            }
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    /**
     * Užtikrina, kad default shop_markers egzistuotų Firestore
     */
    private void initDefaultShopMarkers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> defaultMarkers = new ArrayList<>();

        Map<String, Object> violetMarker = new HashMap<>();
        violetMarker.put("name", "Violetinis markeris");
        violetMarker.put("price", 10);
        violetMarker.put("drawable", "marker_violet");
        defaultMarkers.add(violetMarker);

        Map<String, Object> redMarker = new HashMap<>();
        redMarker.put("name", "Raudonas markeris");
        redMarker.put("price", 20);
        redMarker.put("drawable", "marker_red");
        defaultMarkers.add(redMarker);

        Map<String, Object> blueMarker = new HashMap<>();
        blueMarker.put("name", "Mėlynas markeris");
        blueMarker.put("price", 30);
        blueMarker.put("drawable", "marker_blue");
        defaultMarkers.add(blueMarker);

        Map<String, Object> googleMarker = new HashMap<>();
        googleMarker.put("name", "Google markeris");
        googleMarker.put("price", 30);
        googleMarker.put("drawable", "google");
        defaultMarkers.add(googleMarker);

        for (Map<String, Object> marker : defaultMarkers) {
            String markerName = (String) marker.get("name");
            db.collection("shop_markers")
                    .whereEqualTo("name", markerName)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            // Sukuriame tik jei neegzistuoja
                            db.collection("shop_markers")
                                    .add(marker)
                                    .addOnSuccessListener(docRef -> Log.d("HomeActivity", "Markeris sukurtas: " + markerName))
                                    .addOnFailureListener(e -> Log.e("HomeActivity", "Nepavyko sukurti markerio: " + markerName, e));
                        } else {
                            Log.d("HomeActivity", "Markeris jau egzistuoja: " + markerName);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("HomeActivity", "Klaida tikrinant markerį: " + markerName, e));
        }
    }

}
