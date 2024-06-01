package com.example.projectkrs.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectkrs.R;
import com.example.projectkrs.activities.ChangePasswordActivity;
import com.example.projectkrs.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView textViewUserEmail;
    private Button buttonChangePassword, buttonLogout;
    private static final int CHANGE_PASSWORD_REQUEST = 1;

    // konstruktorius
    public ProfileFragment() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Password pakeitimas sėkmingas
            } else {
                // Password keitimas nepavyko arba buvo atšauktas
                Toast.makeText(getActivity(), "Slaptažodžio keitimas nepavyko arba buvo atšauktas", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicijuoti UI komponentus
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Gauti dabartini naudotoją
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Gauti naudotojo El. Paštą
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                // Nustatyti naudotojo El. Paštą į TextView
                textViewUserEmail.setText(userEmail);
            }
        }

        // Listeneris pakeistį slaptažodį
        buttonChangePassword.setOnClickListener(v -> {
            // Paleisti ChangePasswordActivity
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivityForResult(intent, CHANGE_PASSWORD_REQUEST);
        });


        // Listeneris atsijungti
        buttonLogout.setOnClickListener(v -> {
            // Atjungti naudotoją iš Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Rodyti mainactivity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Atsijungimo toast
            Toast.makeText(getActivity(), "Jūs sėkmingai atsijungėte. ", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
