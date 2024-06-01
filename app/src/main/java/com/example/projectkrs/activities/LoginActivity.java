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

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin;
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth firebaseAuth;
    private TextView textViewSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonLogin = findViewById(R.id.buttonLogin);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewSignUp = findViewById(R.id.textViewSignUp);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Prašome suvesti visus duomenų laukus", Toast.LENGTH_SHORT).show();
                return;
            }

            // Naudotojo autentifikacija su firebase
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Prisijungimas sėkmingas
                            Toast.makeText(LoginActivity.this, "Prisijungimas sėkmingas", Toast.LENGTH_SHORT).show();

                            // Pereiti į "HomeActivity"
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Close LoginActivity kad naudotojas negalėtu grižti atgal
                        } else {
                            // Nepavyko prisijungti
                            Toast.makeText(LoginActivity.this, "Prisijungti nepavyko",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        });
        textViewSignUp.setOnClickListener(v -> {
            // Atidaryti registracijos activity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
