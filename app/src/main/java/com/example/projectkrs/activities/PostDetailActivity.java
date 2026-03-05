package com.example.projectkrs.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.example.projectkrs.adapters.RecommendationAdapter;
import com.example.projectkrs.model.Comment;
import com.example.projectkrs.model.PlaceRecommendation;
import com.example.projectkrs.utils.UserBackgroundHelper;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private Place place;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();

    private RecyclerView recyclerViewRecommendations;
    private RecommendationAdapter recommendationAdapter;
    private List<PlaceRecommendation> recommendationList = new ArrayList<>();

    private Button btnWant, btnVisited;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        UserBackgroundHelper.applySelectedBackground(this);

        place = getIntent().getParcelableExtra("place");
        if (place == null) { finish(); return; }

        ImageView imageView = findViewById(R.id.imageView);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        TextView textViewOpeningHours = findViewById(R.id.textViewOpeningHours);
        Button openMapsButton = findViewById(R.id.openMapsButton);
        btnWant = findViewById(R.id.btnWant);
        btnVisited = findViewById(R.id.btnVisited);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Turite prisijungti", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // RecyclerView Comments
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        // RecyclerView Recommendations
        recyclerViewRecommendations = findViewById(R.id.recyclerViewRecommendations);
        recyclerViewRecommendations.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recommendationAdapter = new RecommendationAdapter(
                this,
                recommendationList,
                placeRecommendation -> {
                    Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                    intent.putExtra("placeRecommendation", placeRecommendation);
                    startActivity(intent);
                }
        );
        recyclerViewRecommendations.setAdapter(recommendationAdapter);

        // Place info
        textViewTitle.setText(place.getName());
        textViewDescription.setText(place.getAddress());
        textViewOpeningHours.setText(getIntent().getBooleanExtra("isOpenNow", false) ? "Atidaryta" : "Uždaryta");

        // Load Image
        loadPlaceMainImage(imageView);

        // Google Maps button
        openMapsButton.setOnClickListener(v -> {
            if (place.getLatLng() != null) {
                double lat = place.getLatLng().latitude;
                double lng = place.getLatLng().longitude;
                Uri gmmIntentUri = Uri.parse(
                        "geo:" + lat + "," + lng +
                                "?q=" + lat + "," + lng +
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

        // Buttons
        btnWant.setOnClickListener(v -> savePlaceStatus("want"));
        btnVisited.setOnClickListener(v -> savePlaceStatus("visited"));

        // Load comments
        loadGoogleComments(place.getId());

        // Disable visited button if already visited
        checkIfAlreadyVisited();

        // Load nearby recommendations
        loadNearbyRecommendations();
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

        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if (photos != null && !photos.isEmpty()) {
            List<String> photoRefs = new ArrayList<>();
            for (PhotoMetadata photo : photos) photoRefs.add(photo.zzb());
            data.put("photoReferences", photoRefs);
        }

        db.collection("users")
                .document(userId)
                .collection("places")
                .document(place.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    boolean firstVisit = !doc.contains("visitedPointsAdded");

                    db.collection("users")
                            .document(userId)
                            .collection("places")
                            .document(place.getId())
                            .set(data)
                            .addOnSuccessListener(aVoid -> {

                                if (status.equals("visited") && firstVisit) {
                                    db.collection("users").document(userId)
                                            .update("points", FieldValue.increment(10));

                                    db.collection("users")
                                            .document(userId)
                                            .collection("places")
                                            .document(place.getId())
                                            .update("visitedPointsAdded", true);

                                    btnVisited.setEnabled(false);

                                    showPointsAnimation("+10", R.drawable.ic_diamond);
                                }

                                Toast.makeText(this, "Vieta pažymėta: " + status, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Klaida saugant vietą", Toast.LENGTH_SHORT).show());
                });
    }

    private void showPointsAnimation(String text, int iconResId) {
        FrameLayout rootLayout = findViewById(android.R.id.content);

        FrameLayout animationLayout = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        animationLayout.setLayoutParams(params);
        animationLayout.setBackgroundColor(0x88000000);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconResId);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(300, 300);
        iconParams.gravity = Gravity.CENTER;
        icon.setLayoutParams(iconParams);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(48f);
        tv.setTextColor(0xFFFFFFFF);
        tv.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tvParams.gravity = Gravity.CENTER;
        tv.setLayoutParams(tvParams);

        animationLayout.addView(icon);
        animationLayout.addView(tv);
        rootLayout.addView(animationLayout);

        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(300);
        Animation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.setStartOffset(1200);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { animationLayout.startAnimation(fadeOut); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { rootLayout.removeView(animationLayout); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });

        animationLayout.startAnimation(fadeIn);
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
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Klaida gaunant komentarus", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }

    // Horizontal recommendations loader
    private void loadNearbyRecommendations() {
        if (place.getLatLng() == null) return;
        double lat = place.getLatLng().latitude;
        double lng = place.getLatLng().longitude;

        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + lat + "," + lng +
                        "&radius=5000" +
                        "&language=lt" +
                        "&type=tourist_attraction" +
                        "&key=" + getString(R.string.places_api_key);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }

                JSONArray results = new JSONObject(response.toString()).getJSONArray("results");
                runOnUiThread(() -> {
                    recommendationList.clear();
                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject obj = results.getJSONObject(i);
                            String name = obj.getString("name");
                            JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                            String placeId = obj.getString("place_id");
                            String address = obj.optString("vicinity", "");
                            String photoUrl = null;

                            JSONArray photos = obj.optJSONArray("photos");
                            if (photos != null && photos.length() > 0) {
                                JSONObject photoObj = photos.getJSONObject(0);
                                String photoRef = photoObj.getString("photo_reference");
                                photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                                        + photoRef + "&key=" + getString(R.string.places_api_key);
                            }

                            PlaceRecommendation rec = new PlaceRecommendation(
                                    name, placeId, address,
                                    loc.getDouble("lat"), loc.getDouble("lng"),
                                    photoUrl
                            );
                            recommendationList.add(rec);
                        } catch (Exception ignored) {}
                    }
                    recommendationAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
