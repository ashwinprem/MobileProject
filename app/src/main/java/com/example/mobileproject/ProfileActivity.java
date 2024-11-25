package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView userEmail;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        userEmail = findViewById(R.id.userEmail);
        logoutButton = findViewById(R.id.logoutButton);

        // Display user's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail.setText("Logged in as: " + currentUser.getEmail());
        } else {
            userEmail.setText("No user logged in.");
        }

        // Set up Logout button
        logoutButton.setOnClickListener(v -> {
            Log.d(TAG, "Logout button clicked."); // Debug log
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
