package com.example.projectkrs.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.projectkrs.R;
import com.example.projectkrs.adapters.ShopAdapter;
import com.example.projectkrs.model.ShopMarker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ShopFragment extends Fragment {

    public interface OnBackgroundChangeListener {
        void onBackgroundChanged(String drawableName);
    }

    private OnBackgroundChangeListener backgroundListener;

    private RecyclerView recyclerView;
    private TextView textPoints;

    private FirebaseFirestore db;
    private String userId;
    private int userPoints = 0;

    private final List<ShopMarker> items = new ArrayList<>();
    private ShopAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnBackgroundChangeListener) {
            backgroundListener = (OnBackgroundChangeListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.shopRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        textPoints = view.findViewById(R.id.textPoints);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        adapter = new ShopAdapter(items, this::onItemClicked);
        recyclerView.setAdapter(adapter);

        loadUserPoints();
        loadAllShopItems();

        return view;
    }

    private void loadUserPoints() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("points")) {
                        userPoints = doc.getLong("points").intValue();
                    }
                    textPoints.setText(formatPointsText(userPoints));
                });
    }

    private void loadAllShopItems() {

        items.clear();

        db.collection("shop_markers").get()
                .addOnSuccessListener(snapshot -> {

                    for (DocumentSnapshot doc : snapshot) {
                        ShopMarker item = doc.toObject(ShopMarker.class);
                        if (item != null) items.add(item);
                    }

                    db.collection("shop_backgrounds").get()
                            .addOnSuccessListener(bgSnap -> {

                                for (DocumentSnapshot doc : bgSnap) {
                                    ShopMarker item = doc.toObject(ShopMarker.class);
                                    if (item != null) items.add(item);
                                }

                                addLocalMapStyleItemsIfMissing();
                                adapter.notifyDataSetChanged();
                            });
                });
    }


    private void addLocalMapStyleItemsIfMissing() {
        addLocalShopItemIfMissing("Assasins Creed žemėlapis", 120, "map_assasinscreed");
        addLocalShopItemIfMissing("Desert žemėlapis", 120, "map_desert");
        addLocalShopItemIfMissing("Night Vision žemėlapis", 120, "map_nightvision");
        addLocalShopItemIfMissing("San Andreas žemėlapis", 120, "map_sanandreas");
    }

    private void addLocalShopItemIfMissing(String name, int price, String drawable) {
        for (ShopMarker existing : items) {
            if (drawable.equals(existing.getDrawable())) {
                return;
            }
        }

        ShopMarker item = new ShopMarker();
        item.setName(name);
        item.setPrice(price);
        item.setDrawable(drawable);
        items.add(item);
    }

    static String formatPointsText(int points) {
        return "Taškai: " + points;
    }

    static boolean canAffordPurchase(int userPoints, int itemPrice) {
        return userPoints >= itemPrice;
    }

    static boolean isMarkerDrawable(String drawableName) {
        return drawableName != null && drawableName.contains("marker");
    }

    static boolean isMapStyleResource(String drawableName) {
        return drawableName != null && drawableName.startsWith("map_");
    }

    static Map<String, Object> buildPurchaseUpdate(int updatedPoints, String drawableName) {
        Map<String, Object> update = new HashMap<>();
        update.put("points", updatedPoints);

        if (isMarkerDrawable(drawableName)) {
            update.put("selectedMarker", drawableName);
        } else if (isMapStyleResource(drawableName)) {
            update.put("selectedMapStyle", drawableName);
        } else {
            update.put("selectedBackground", drawableName);
        }

        return update;
    }

    private void onItemClicked(ShopMarker item) {

        if (!canAffordPurchase(userPoints, item.getPrice())) {
            Toast.makeText(getContext(), "Nepakanka taškų", Toast.LENGTH_SHORT).show();
            return;
        }

        userPoints -= item.getPrice();
        textPoints.setText(formatPointsText(userPoints));

        Map<String, Object> update = buildPurchaseUpdate(userPoints, item.getDrawable());

        if (!isMarkerDrawable(item.getDrawable()) && !isMapStyleResource(item.getDrawable())) {
            // 🔥 LIVE BACKGROUND KEITIMAS
            if (backgroundListener != null) {
                backgroundListener.onBackgroundChanged(item.getDrawable());
            }
        }

        db.collection("users").document(userId)
                .update(update)
                .addOnSuccessListener(v ->
                        Toast.makeText(getContext(),
                                "Nupirkta: " + item.getName(),
                                Toast.LENGTH_SHORT).show());
    }
}
