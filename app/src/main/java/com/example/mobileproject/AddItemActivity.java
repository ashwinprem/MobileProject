package com.example.mobileproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private EditText itemName, itemDescription, itemPrice, imageUrlInput;
    private Button submitButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize UI components
        itemName = findViewById(R.id.itemName);
        itemDescription = findViewById(R.id.itemDescription);
        itemPrice = findViewById(R.id.itemPrice);
        imageUrlInput = findViewById(R.id.imageUrlInput); // Field for entering image URL
        submitButton = findViewById(R.id.submitButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> addItemToFirestore());

        // Enable back button in Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addItemToFirestore() {
        // Get input values
        String name = itemName.getText().toString().trim();
        String description = itemDescription.getText().toString().trim();
        String price = itemPrice.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create item data map
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("description", description);
        item.put("price", Double.parseDouble(price));
        item.put("imageUrl", imageUrl);

        // Add item to Firestore
        db.collection("items").add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddItemActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after successful submission
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button click
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
