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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private Place place;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        place = getIntent().getParcelableExtra("place");
        boolean isOpenNow = getIntent().getBooleanExtra("isOpenNow", false);

        ImageView imageView = findViewById(R.id.imageView);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        TextView textViewOpeningHours = findViewById(R.id.textViewOpeningHours);

        Button openMapsButton = findViewById(R.id.openMapsButton);
        Button btnWant = findViewById(R.id.btnWant);
        Button btnVisited = findViewById(R.id.btnVisited);

        // ===== Google Maps =====
        openMapsButton.setOnClickListener(v -> {
            if (place != null && place.getLatLng() != null) {
                double lat = place.getLatLng().latitude;
                double lng = place.getLatLng().longitude;

                Uri gmmIntentUri = Uri.parse(
                        "geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + place.getName() + ")"
                );
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps nerasta", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (place == null) {
            Log.e("PostDetailActivity", "Place is null");
            finish();
            return;
        }

        // ===== Place info =====
        textViewTitle.setText(place.getName());
        textViewDescription.setText(place.getAddress());

        if (getIntent().hasExtra("isOpenNow")) {
            textViewOpeningHours.setText(isOpenNow ? "Atidaryta" : "Uždaryta");
        } else {
            textViewOpeningHours.setVisibility(GONE);
        }

        // ===== Image =====
        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if (photos != null && !photos.isEmpty()) {
            String photoReference = photos.get(0).zzb();
            String imageUrl =
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800"
                            + "&photoreference=" + photoReference
                            + "&key=" + getString(R.string.places_api_key);

            Glide.with(this).load(imageUrl).into(imageView);
        } else {
            Glide.with(this).load(R.drawable.no_image_placeholder).into(imageView);
        }

        // ===== CATEGORY BUTTONS =====
        btnWant.setOnClickListener(v -> savePlaceStatus("want"));
        btnVisited.setOnClickListener(v -> savePlaceStatus("visited"));
    }

    // ===== FIRESTORE SAVE =====
    private void savePlaceStatus(String status) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Turite prisijungti", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("placeId", place.getId());
        data.put("name", place.getName());
        data.put("address", place.getAddress());
        data.put("status", status);
        data.put("lat", place.getLatLng().latitude);
        data.put("lng", place.getLatLng().longitude);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("places")
                .document(place.getId())
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                status.equals("want")
                                        ? "Pridėta į „Noriu aplankyti“"
                                        : "Pažymėta kaip „Aplankyta\"",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Klaida saugant", Toast.LENGTH_SHORT).show()
                );
    }
}
