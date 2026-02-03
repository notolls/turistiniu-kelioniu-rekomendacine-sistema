package com.example.projectkrs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectkrs.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin;
    private EditText editTextEmail, editTextPassword;
    private TextView textViewSignUp;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonLogin = findViewById(R.id.buttonLogin);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewSignUp = findViewById(R.id.textViewSignUp);

        buttonLogin.setOnClickListener(v -> loginUser());

        textViewSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Prašome suvesti visus duomenis",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    createUserIfNotExists();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Prisijungti nepavyko",
                                Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * 🔥 SVARBIAUSIA DALIS
     * Sukuria users/{uid}, jei jo dar nėra
     */
    private void createUserIfNotExists() {
        String uid = firebaseAuth.getUid();

        if (uid == null) return;

        DocumentReference userRef =
                db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(doc -> {

            if (!doc.exists()) {
                // 🔹 KURIAM NAUJĄ USER
                Map<String, Object> userData = new HashMap<>();
                userData.put("points", 100);
                userData.put("selectedMarker", "marker_default");

                userRef.set(userData)
                        .addOnSuccessListener(v -> {

                            // 🔹 DEFAULT MARKER
                            userRef.collection("owned_markers")
                                    .document("marker_default")
                                    .set(Collections.singletonMap("owned", true))
                                    .addOnSuccessListener(x -> openMain());

                        });

            } else {
                // 🔹 USER JAU YRA
                openMain();
            }
        });
    }

    private void openMain() {
        Toast.makeText(this,
                "Prisijungimas sėkmingas",
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
