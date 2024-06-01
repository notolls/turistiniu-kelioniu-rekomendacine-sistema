package com.example.projectkrs.activities;

import static android.view.View.GONE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.projectkrs.R;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Gauti Place object iš intento
        Place place = getIntent().getParcelableExtra("place");
        boolean isOpenNow = getIntent().getBooleanExtra("isOpenNow", false);

        Button openMapsButton = findViewById(R.id.openMapsButton);
        openMapsButton.setOnClickListener(v -> {
            if (place != null && place.getLatLng() != null) {
                // Extract latitude and longitude coordinates from the Place object
                double latitude = place.getLatLng().latitude;
                double longitude = place.getLatLng().longitude;

                // Sukurti Intent atidaryti žemėlapius su vietos koordinatėmis
                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + place.getName() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Naudoti google maps
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // jei naudotojas neturi google maps
                    Toast.makeText(getApplicationContext(), "Neturite Google Maps programos.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("PostDetailActivity", "No Place object found in intent extras");
            }
        });


        if (place != null) {
            // Nustatyti rodinius
            ImageView imageView = findViewById(R.id.imageView);
            TextView textViewTitle = findViewById(R.id.textViewTitle);
            TextView textViewDescription = findViewById(R.id.textViewDescription);

            // Nustatyti paveikslėlį, pavadinimą ir adresą
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // Photo metadata is available
                PhotoMetadata photoMetadata = photoMetadataList.get(0); // Gaunama pirma nuotrauka
                String photoReference = photoMetadata.zzb(); // Gaunamas photo reference
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + getString(R.string.places_api_key);
                Glide.with(this).load(imageUrl).into(imageView);
            } else {
                // Nėra nuotraukos, Nustatomas placeholder image
                Glide.with(this).load(R.drawable.no_image_placeholder).into(imageView);
            }
            textViewTitle.setText(place.getName());
            textViewDescription.setText(place.getAddress());
            TextView textViewOpeningHours = findViewById(R.id.textViewOpeningHours);
            //Tikrinamas darbo laikas
            boolean isOpenNowAvailable = getIntent().hasExtra("isOpenNow");

            if (isOpenNowAvailable) {
                textViewOpeningHours.setText(isOpenNow ? "Atidaryta" : "Uždaryta");
            } else {
                // Jei informacijos nėra hide TextView
                textViewOpeningHours.setVisibility(GONE);
            }
        } else {
            Log.e("PostDetailActivity", "No Place object found in intent extras");
        }
    }
}
