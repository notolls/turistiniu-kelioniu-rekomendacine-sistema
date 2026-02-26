package com.example.projectkrs.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.projectkrs.R;
import com.example.projectkrs.adapters.CommentAdapter;
import com.example.projectkrs.model.Comment;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private Place place;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();

    private Button btnWant, btnVisited;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        place = getIntent().getParcelableExtra("place");
        if (place == null) {
            finish();
            return;
        }

        ImageView imageView = findViewById(R.id.imageView);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        TextView textViewOpeningHours = findViewById(R.id.textViewOpeningHours);
        Button openMapsButton = findViewById(R.id.openMapsButton);
        btnWant = findViewById(R.id.btnWant);
        btnVisited = findViewById(R.id.btnVisited);

        // Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Turite prisijungti", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // ===== RecyclerView Comments =====
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        // ===== Place Info =====
        textViewTitle.setText(place.getName());
        textViewDescription.setText(place.getAddress());
        textViewOpeningHours.setText(getIntent().getBooleanExtra("isOpenNow", false) ? "Atidaryta" : "Uždaryta");

        // ===== Load Image =====
        loadPlaceMainImage(imageView);

        // ===== Google Maps Button =====
        openMapsButton.setOnClickListener(v -> {
            if (place.getLatLng() != null) {
                Uri gmmIntentUri = Uri.parse(
                        "geo:" + place.getLatLng().latitude + "," + place.getLatLng().longitude +
                                "?q=" + place.getLatLng().latitude + "," + place.getLatLng().longitude +
                                "(" + place.getName() + ")"
                );
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(mapIntent);
                else
                    Toast.makeText(this, "Google Maps nerasta", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== Button Listeners =====
        btnWant.setOnClickListener(v -> savePlaceStatus("want"));
        btnVisited.setOnClickListener(v -> savePlaceStatus("visited"));

        // ===== Load Google Comments =====
        loadGoogleComments(place.getId());

        // ===== Disable btnVisited if already visited =====
        checkIfAlreadyVisited();
    }

    private void loadPlaceMainImage(ImageView imageView) {
        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if (photos != null && !photos.isEmpty()) {
            String photoReference = photos.get(0).zzb();
            String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800"
                    + "&photoreference=" + photoReference
                    + "&key=" + getString(R.string.places_api_key);
            Glide.with(this).load(imageUrl).into(imageView);
        } else {
            Glide.with(this).load(R.drawable.no_image_placeholder).into(imageView);
        }
    }

    private void savePlaceStatus(String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("placeId", place.getId());
        data.put("name", place.getName());
        data.put("address", place.getAddress());
        data.put("status", status);
        data.put("lat", place.getLatLng().latitude);
        data.put("lng", place.getLatLng().longitude);

        // Save photoReferences
        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if (photos != null && !photos.isEmpty()) {
            List<String> photoRefs = new ArrayList<>();
            for (PhotoMetadata photo : photos) {
                photoRefs.add(photo.zzb());
            }
            data.put("photoReferences", photoRefs);
        }

        db.collection("users")
                .document(userId)
                .collection("places")
                .document(place.getId())
                .set(data)
                .addOnSuccessListener(aVoid -> {

                    // ✅ Add 10 points only if "visited" and first time
                    if (status.equals("visited")) {
                        db.collection("users")
                                .document(userId)
                                .collection("places")
                                .document(place.getId())
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (!doc.contains("visitedPointsAdded")) {
                                        // Add points
                                        db.collection("users").document(userId)
                                                .update("points", FieldValue.increment(10));
                                        // Mark that points were added
                                        db.collection("users")
                                                .document(userId)
                                                .collection("places")
                                                .document(place.getId())
                                                .update("visitedPointsAdded", true);
                                        btnVisited.setEnabled(false); // disable button
                                    }
                                });
                    }

                    Toast.makeText(this, "Vieta pažymėta: " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Klaida saugant vietą", Toast.LENGTH_SHORT).show());
    }

    private void checkIfAlreadyVisited() {
        db.collection("users")
                .document(userId)
                .collection("places")
                .document(place.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("visitedPointsAdded")) {
                        btnVisited.setEnabled(false);
                    }
                });
    }

    private void loadGoogleComments(String placeId) {
        String url = "https://maps.googleapis.com/maps/api/place/details/json?place_id="
                + placeId + "&fields=name,reviews&key=" + getString(R.string.places_api_key);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");
                        JSONArray reviews = result.optJSONArray("reviews");
                        if (reviews != null) {
                            commentList.clear();
                            for (int i = 0; i < reviews.length(); i++) {
                                JSONObject r = reviews.getJSONObject(i);
                                String author = r.getString("author_name");
                                String text = r.getString("text");
                                float rating = (float) r.getDouble("rating");
                                commentList.add(new Comment(author, text, rating));
                            }
                            commentAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Klaida gaunant komentarus", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
}