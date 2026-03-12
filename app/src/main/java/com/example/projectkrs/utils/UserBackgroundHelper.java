package com.example.projectkrs.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class UserBackgroundHelper {

    private UserBackgroundHelper() {
    }

    static boolean hasUsableBackgroundSelection(String drawableName) {
        return drawableName != null && !drawableName.trim().isEmpty();
    }

    static boolean isValidDrawableResource(int resId) {
        return resId != 0;
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
                    if (!hasUsableBackgroundSelection(drawableName)) {
                        return;
                    }

                    int resId = activity.getResources().getIdentifier(
                            drawableName,
                            "drawable",
                            activity.getPackageName()
                    );

                    if (isValidDrawableResource(resId)) {
                        activity.findViewById(android.R.id.content).setBackgroundResource(resId);
                    }
                });
    }
}
