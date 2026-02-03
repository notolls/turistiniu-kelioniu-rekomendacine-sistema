package com.example.projectkrs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectkrs.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonRegister;
    private FirebaseAuth firebaseAuth;
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private Map<String, String> categoryTypesMap;
    private TextView textViewSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewSignIn = findViewById(R.id.textViewSignIn);
        categorySpinner = findViewById(R.id.categorySpinner);

        // Kategorijų sąrašas
        categoryTypesMap = new HashMap<>();
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Autobusų stotys", "bus_station");
        categoryTypesMap.put("Traukinių stotys", "train_station");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Prekybos centrai", "shopping_mall");
        categoryTypesMap.put("Bendros lankytinos vietos", "tourist_attraction");

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(categoryTypesMap.keySet()));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        buttonRegister.setOnClickListener(v -> registerUser());

        textViewSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        final String selectedCategory;

        if (categorySpinner.getSelectedItem() != null) {
            selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
        } else {
            Toast.makeText(this, "Pasirinkite kategoriją", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            createUserInFirestore(user.getUid(), selectedCategory);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registracija nepavyko", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserInFirestore(String uid, String categoryType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("categoryType", categoryType);
        userData.put("points", 100); // pradiniai taškai
        userData.put("selectedMarker", "marker_default"); // pradinis markeris

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Vartotojas sukurtas!", Toast.LENGTH_SHORT).show();

                    // Pereiname į HomeActivity (ten bus shop_markers inicializacija)
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Klaida saugant vartotoją", Toast.LENGTH_SHORT).show());
    }
}
