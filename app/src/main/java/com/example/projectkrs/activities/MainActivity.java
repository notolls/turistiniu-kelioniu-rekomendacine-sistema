package com.example.projectkrs.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projectkrs.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Patikrinti ar naudotojas jau prisijungęs
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Naudotojas jau prisijunges rodyti splash ekraną
            setContentView(R.layout.splash_screen);

        } else {
            // Naudotojas neprisijungęs rodyti autentifikacijos logiką
            setContentView(R.layout.activity_main);
        }
        // Inicijuoti FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Prašyti leidimo nustatyti vietą
        requestLocationPermission();

    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                // Tikrinti ar naudotojas prisijungęs
                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    // Prisijunges eiti į "Homeactivity"
                                    Log.d("openning home", "User location: " + userLocation.latitude + ", " + userLocation.longitude);
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    intent.putExtra("user_location", userLocation);
                                    startActivity(intent);
                                    finish();
                                }  // neprisijungęs pasilikti "MainActivity"

                            }  // Location is null

                        })
                        .addOnFailureListener(e -> {
                            //nepavyko gauti vietos
                            Toast.makeText(MainActivity.this, "Nepavyko gauti buvimo vietos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } catch (SecurityException e) {
                Log.e("MainActivity", "Security Exception occurred", e);
            }
        }
    }


    // Leidimo užklausos rezultato tvarkymas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                //nesuteikta prieiga
                Toast.makeText(MainActivity.this, "Reikia jusų buvimo vietos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Prisijungimo mygtuko paspaudimo apdorojimo metodas
    public void loginOnClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Registracijos mygtuko paspaudimo apdorojimo metodas
    public void signUpOnClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
