package com.example.projectkrs.activities;

import android.os.Bundle;
import android.text.TextUtils;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectkrs.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextCurrentPassword, editTextNewPassword;
    private Button buttonChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // inicijuoti UI komponentus
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        // listeneris pakeisti slaptažodį mygtukui
        buttonChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        // Get dabartinį ir naują slaptažodį iš Edittext
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();

        // Tikrinam įvesti
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(ChangePasswordActivity.this, "Prašome įvesti darbtinį ir naują slaptažodžius", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get esamą naudotoją
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // išnaujo autentifikuojame naudotoją su dabartiniu slaptažodžiu
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Atnaujiname naudotojo slaptažodį
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    // Sėkmingai pakeistas
                                    Toast.makeText(ChangePasswordActivity.this, "Slaptažodis sėkmingai pakeistas.", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Klaida keičiant slaptažodį
                                    Toast.makeText(ChangePasswordActivity.this, "Nepavyko pakeisti slaptažodžio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Nepavyko išnaujo autentifikuoti naudotojo
                        Toast.makeText(ChangePasswordActivity.this, "Klaidingas dabartinis slaptažodis: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
