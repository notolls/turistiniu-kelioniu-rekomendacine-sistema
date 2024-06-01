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

        // Inicijuojamas Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewSignIn = findViewById(R.id.textViewSignIn);
        categorySpinner = findViewById(R.id.categorySpinner);

        // Inicijuojamas kategorijų pasirinkimas HashMap
        categoryTypesMap = new HashMap<>();
        categoryTypesMap.put("Kavinės", "cafe");
        categoryTypesMap.put("Restoranai", "restaurant");
        categoryTypesMap.put("Muziejai", "museum");
        categoryTypesMap.put("Autobusų stotys", "bus_station");
        categoryTypesMap.put("Traukinių stotys", "train_station");
        categoryTypesMap.put("Parkai", "park");
        categoryTypesMap.put("Prekybos centrai", "shopping_mall");
        categoryTypesMap.put("Bendros lankytinos vietos", "tourist_attraction");

        // Inicijuojamas ArrayAdapter su kategorijomis
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(categoryTypesMap.keySet()));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(categoryAdapter);

        buttonRegister.setOnClickListener(v -> registerUser());
        textViewSignIn.setOnClickListener(v -> {
            // Atidaryti prisijungimo activity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        final String selectedCategory;

        // Gauti pasirinkta kategoriją iš spinner
        if (categorySpinner.getSelectedItem() != null) {
            selectedCategory = categoryTypesMap.get(categorySpinner.getSelectedItem().toString());
        } else {
            //nepasirinkta
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

        // Naudotojo registracija su firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registracija sėkminga
                        Toast.makeText(RegisterActivity.this, "Registracija sėkminga.", Toast.LENGTH_SHORT).show();

                        // Išsaugoti pasirinkta kategorija firestore db
                        saveCategoryTypeToFirestore(selectedCategory);

                        // Pereiti į "HomeActivity"
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // // Close RegisterActivity kad naudotojas negalėtu grižti atgal
                    } else {
                        // Registracija nepavyko
                        Toast.makeText(RegisterActivity.this, "Registracija nepavyko", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // pasirinktos kategorijos išsaugojimo metodas
    private void saveCategoryTypeToFirestore(String categoryType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("categoryType", categoryType);

            db.collection("users").document(user.getUid()).set(userData)
                    .addOnSuccessListener(aVoid -> {
                        //sėkminga
                    })
                    .addOnFailureListener(e -> {
                        //nepavyko
                    });
        }
    }
}
