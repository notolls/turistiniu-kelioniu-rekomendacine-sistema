package com.example.projectkrs.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class UserBackgroundHelper {

    private UserBackgroundHelper() {
    }

    public static void applySelectedBackground(@NonNull AppCompatActivity activity) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || !doc.contains("selectedBackground")) {
                        return;
                    }

                    String drawableName = doc.getString("selectedBackground");
                    if (drawableName == null || drawableName.trim().isEmpty()) {
                        return;
                    }

                    int resId = activity.getResources().getIdentifier(
                            drawableName,
                            "drawable",
                            activity.getPackageName()
                    );

                    if (resId != 0) {
                        activity.findViewById(android.R.id.content).setBackgroundResource(resId);
                    }
                });
    }
}
