package com.example.projectkrs.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.projectkrs.fragments.ChatFragment;
import com.example.projectkrs.R;
import com.example.projectkrs.activities.ChangePasswordActivity;
import com.example.projectkrs.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView textViewUserEmail;
    private Button buttonChangePassword, buttonLogout;
    private LinearLayout achievementsContainer;

    private static final int CHANGE_PASSWORD_REQUEST = 1;

    // 🔹 PAVYZDINIAI DUOMENYS (vėliau – iš Firebase)
    private int visitedPlaces = 35;

    private boolean boughtRedMarker = true;
    private boolean boughtBlueMarker = false;
    private boolean boughtPurpleMarker = true;

    public ProfileFragment() {}

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (shouldShowChangePasswordFailureToast(requestCode, resultCode)) {
            Toast.makeText(
                    getActivity(),
                    "Slaptažodžio keitimas nepavyko arba buvo atšauktas",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        achievementsContainer = view.findViewById(R.id.achievementsContainer);

        Button buttonOpenChat = view.findViewById(R.id.buttonOpenChat);

        buttonOpenChat.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .addToBackStack(null)
                    .commit();
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            textViewUserEmail.setText(currentUser.getEmail());
        }

        buttonChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivityForResult(intent, CHANGE_PASSWORD_REQUEST);
        });

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(getActivity(), "Atsijungta sėkmingai", Toast.LENGTH_SHORT).show();
        });

        loadAchievements();
        return view;
    }


    static boolean shouldShowChangePasswordFailureToast(int requestCode, int resultCode) {
        return requestCode == CHANGE_PASSWORD_REQUEST && resultCode != Activity.RESULT_OK;
    }

    static boolean isAchievementUnlockedByVisitedPlaces(int visitedPlaces, int threshold) {
        return visitedPlaces >= threshold;
    }

    // ======================
    // 🏆 ACHIEVEMENTAI
    // ======================
    private void loadAchievements() {

        achievementsContainer.removeAllViews();

        // 🎨 MARKERIAI
        addAchievement(
                "Raudonas menininkas",
                "Nusipirkai raudoną markerį",
                boughtRedMarker
        );

        addAchievement(
                "Mėlynas strategas",
                "Nusipirkai mėlyną markerį",
                boughtBlueMarker
        );

        addAchievement(
                "Violetinis vizionierius",
                "Nusipirkai violetinį markerį",
                boughtPurpleMarker
        );

        // 🌍 KELIONĖS
        addAchievement("Keliautojas I", "Aplankyta 10 vietų", isAchievementUnlockedByVisitedPlaces(visitedPlaces, 10));
        addAchievement("Keliautojas II", "Aplankyta 20 vietų", isAchievementUnlockedByVisitedPlaces(visitedPlaces, 20));
        addAchievement("Keliautojas III", "Aplankyta 30 vietų", isAchievementUnlockedByVisitedPlaces(visitedPlaces, 30));
        addAchievement("Keliautojas IV", "Aplankyta 40 vietų", isAchievementUnlockedByVisitedPlaces(visitedPlaces, 40));
        addAchievement("Keliautojas V", "Aplankyta 50 vietų", isAchievementUnlockedByVisitedPlaces(visitedPlaces, 50));
        addAchievement("Legenda", "Aplankyta 60 vietų", isAchievementUnlockedByVisitedPlaces(visitedPlaces, 60));

        // ⭐ PAPILDOMI
        addAchievement("Pirmas prisijungimas", "Prisijungei pirmą kartą", true);
        addAchievement("Profilio tyrinėtojas", "Aplankei profilio langą", true);
    }

    private void addAchievement(String title, String description, boolean unlocked) {

        View item = LayoutInflater.from(getContext())
                .inflate(R.layout.item_achievement, achievementsContainer, false);

        ImageView icon = item.findViewById(R.id.achievementIcon);
        TextView titleView = item.findViewById(R.id.achievementTitle);
        TextView descView = item.findViewById(R.id.achievementDescription);

        titleView.setText(title);
        descView.setText(description);

        if (unlocked) {
            icon.setImageResource(android.R.drawable.star_big_on);

            item.setAlpha(0f);
            item.post(() -> {
                item.setAlpha(1f);
                item.startAnimation(
                        AnimationUtils.loadAnimation(
                                getContext(),
                                R.anim.achievement_unlock
                        )
                );
            });
        } else {
            icon.setImageResource(android.R.drawable.ic_lock_lock);
            item.setAlpha(0.5f);
        }

        achievementsContainer.addView(item);
    }
}
