package com.example.projectkrs.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projectkrs.R;
import com.example.projectkrs.fragments.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class HomeActivity extends AppCompatActivity
        implements ShopFragment.OnBackgroundChangeListener {

    private BottomNavigationView bottomNavigationView;
    private LatLng userLocation;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        userLocation = getIntent().getParcelableExtra("user_location");

        initDefaultShopMarkers();
        initDefaultBackgrounds();
        loadUserBackground();

        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            Bundle args = new Bundle();
            args.putParcelable("user_location", userLocation);

            switch (item.getItemId()) {
                case R.id.navigation_home: fragment = new HomeFragment(); break;
                case R.id.navigation_search: fragment = new SearchFragment(); break;
                case R.id.navigation_map: fragment = new MapFragment(); break;
                case R.id.navigation_shop: fragment = new ShopFragment(); break;
                case R.id.navigation_profile: fragment = new ProfileFragment(); break;
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

    // ===========================
    // LIVE BACKGROUND KEITIMAS
    // ===========================
    @Override
    public void onBackgroundChanged(String drawableName) {

        int resId = getResources().getIdentifier(
                drawableName,
                "drawable",
                getPackageName()
        );

        if (resId != 0) {
            findViewById(android.R.id.content)
                    .setBackgroundResource(resId);
        }
    }

    private void loadUserBackground() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && doc.contains("selectedBackground")) {

                        String drawableName = doc.getString("selectedBackground");

                        int resId = getResources().getIdentifier(
                                drawableName,
                                "drawable",
                                getPackageName()
                        );

                        if (resId != 0) {
                            findViewById(android.R.id.content)
                                    .setBackgroundResource(resId);
                        }
                    }
                });
    }

    // ===========================
    // DEFAULT MARKERIAI
    // ===========================
    private void initDefaultShopMarkers() {

        List<Map<String, Object>> markers = new ArrayList<>();
        markers.add(createItem("Violetinis markeris", 10, "marker_violet"));
        markers.add(createItem("Raudonas markeris", 20, "marker_red"));
        markers.add(createItem("Mėlynas markeris", 30, "marker_blue"));

        for (Map<String, Object> marker : markers) {
            db.collection("shop_markers")
                    .whereEqualTo("name", marker.get("name"))
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            db.collection("shop_markers").add(marker);
                        }
                    });
        }
    }

    // ===========================
    // DEFAULT BACKGROUNDAI
    // ===========================
    private void initDefaultBackgrounds() {

        List<Map<String, Object>> backgrounds = new ArrayList<>();
        backgrounds.add(createItem("Žalias gradientas", 25, "green_gradient_background"));
        backgrounds.add(createItem("Mėlynas su taškais", 40, "blue_dots_background"));

        for (Map<String, Object> bg : backgrounds) {
            db.collection("shop_backgrounds")
                    .whereEqualTo("name", bg.get("name"))
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.isEmpty()) {
                            db.collection("shop_backgrounds").add(bg);
                        }
                    });
        }
    }

    private Map<String, Object> createItem(String name, int price, String drawable) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("price", price);
        map.put("drawable", drawable);
        return map;
    }
}