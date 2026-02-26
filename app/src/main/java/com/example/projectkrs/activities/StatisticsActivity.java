package com.example.projectkrs.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectkrs.R;
import com.example.projectkrs.adapters.TopPlaceAdapter;
import com.example.projectkrs.model.PlaceWithCount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvTotalVisited, tvMostVisitedType;
    private ProgressBar progressBarMonthly;
    private RecyclerView recyclerViewTopPlaces;
    private TopPlaceAdapter topPlaceAdapter;
    private List<PlaceWithCount> topPlacesList;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvTotalVisited = findViewById(R.id.tvTotalVisited);
        tvMostVisitedType = findViewById(R.id.tvMostVisitedType);
        progressBarMonthly = findViewById(R.id.progressBarMonthly);
        recyclerViewTopPlaces = findViewById(R.id.recyclerViewTopPlaces);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        recyclerViewTopPlaces.setLayoutManager(new LinearLayoutManager(this));
        topPlacesList = new ArrayList<>();
        topPlaceAdapter = new TopPlaceAdapter(topPlacesList);
        recyclerViewTopPlaces.setAdapter(topPlaceAdapter);

        loadUserStatistics();
        loadTopPlacesAllUsers();
    }

    private void loadUserStatistics() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("places")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int totalVisited = 0;
                        int monthlyVisited = 0;
                        HashMap<String, Integer> typeCounts = new HashMap<>();

                        long currentMonth = System.currentTimeMillis() / (1000L * 60 * 60 * 24 * 30); // rough month

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            String type = doc.getString("type"); // jeigu saugomas tipas
                            Long timestamp = doc.getLong("visitedAt"); // jei saugomas timestamp

                            if ("visited".equals(status)) {
                                totalVisited++;

                                if (timestamp != null && timestamp / (1000L * 60 * 60 * 24 * 30) == currentMonth) {
                                    monthlyVisited++;
                                }

                                if (type != null) {
                                    typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
                                }
                            }
                        }

                        tvTotalVisited.setText("Aplankyta vietų: " + totalVisited);
                        progressBarMonthly.setProgress(monthlyVisited);

                        String mostVisitedType = typeCounts.entrySet().stream()
                                .max(Comparator.comparingInt(java.util.Map.Entry::getValue))
                                .map(java.util.Map.Entry::getKey)
                                .orElse("Nėra duomenų");
                        tvMostVisitedType.setText("Dažniausiai lankytas tipas: " + mostVisitedType);
                    }
                });
    }

    private void loadTopPlacesAllUsers() {
        db.collectionGroup("places")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        HashMap<String, Integer> placeCounts = new HashMap<>();
                        HashMap<String, String> placeNames = new HashMap<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            if ("visited".equals(status)) {
                                String placeId = doc.getId();
                                String name = doc.getString("name");
                                placeCounts.put(placeId, placeCounts.getOrDefault(placeId, 0) + 1);
                                placeNames.put(placeId, name);
                            }
                        }

                        topPlacesList.clear();
                        for (String placeId : placeCounts.keySet()) {
                            topPlacesList.add(new PlaceWithCount(placeNames.get(placeId), placeCounts.get(placeId)));
                        }

                        Collections.sort(topPlacesList, (p1, p2) -> Integer.compare(p2.getCount(), p1.getCount()));
                        topPlaceAdapter.notifyDataSetChanged();
                    }
                });
    }
}
