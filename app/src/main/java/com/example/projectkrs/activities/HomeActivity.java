package com.example.projectkrs.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.projectkrs.fragments.HomeFragment;
import com.example.projectkrs.fragments.ProfileFragment;
import com.example.projectkrs.R;
import com.example.projectkrs.fragments.SearchFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // inicijuoti aptini menu komponentus
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Gauti naudotojo buvimo vieta iš main activity
        userLocation = getIntent().getParcelableExtra("user_location");

        // Listeneriai apatinio menu mygtukams
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    // Sukurti naują „HomeFragment“ instanciją.
                    HomeFragment homeFragment = new HomeFragment();

                    // Nustatyti fragmento parametrus
                    Bundle args = new Bundle();
                    args.putParcelable("user_location", userLocation);
                    homeFragment.setArguments(args);

                    // Pakeisti rodomą fragmentą
                    replaceFragment(homeFragment);
                    return true;
                case R.id.navigation_search:
                    // Sukurti naują „SearchFragment“ instanciją.
                    SearchFragment searchFragment = new SearchFragment();

                    // Nustatyti fragmento parametrus
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user_location", userLocation);
                    searchFragment.setArguments(bundle);

                    // Pakeisti rodomą fragmentą
                    replaceFragment(searchFragment);
                    return true;
                case R.id.navigation_profile:
                    // Pakeisti rodomą fragmentą
                    replaceFragment(new ProfileFragment());
                    return true;
                default:
                    return false;
            }
        });

        // Nustatyti numatytąjį fragmentą, kuris bus rodomas pirmą kartą atidarius HomeActivity
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    // fragmentų pakeitimo metodas
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}