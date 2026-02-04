package com.example.projectkrs.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class StatisticsFragment extends Fragment {

    private TextView tvTotalVisited, tvMostVisitedType;
    private ProgressBar progressBarMonthly;
    private RecyclerView recyclerViewTopPlaces;
    private TopPlaceAdapter topPlaceAdapter;
    private List<PlaceWithCount> topPlacesList;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    public StatisticsFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        tvTotalVisited = view.findViewById(R.id.tvTotalVisited);
        tvMostVisitedType = view.findViewById(R.id.tvMostVisitedType);
        progressBarMonthly = view.findViewById(R.id.progressBarMonthly);
        recyclerViewTopPlaces = view.findViewById(R.id.recyclerViewTopPlaces);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        recyclerViewTopPlaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        topPlacesList = new ArrayList<>();
        topPlaceAdapter = new TopPlaceAdapter(topPlacesList);
        recyclerViewTopPlaces.setAdapter(topPlaceAdapter);

        loadUserStatistics();
        loadTopPlacesAllUsers();

        return view;
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

                        long currentMonth = System.currentTimeMillis() / (1000L * 60 * 60 * 24 * 30);

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String status = doc.getString("status");
                            String type = doc.getString("type");
                            Long timestamp = doc.getLong("visitedAt");

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
