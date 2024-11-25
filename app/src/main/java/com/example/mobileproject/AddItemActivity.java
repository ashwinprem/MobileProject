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
        imageUrlInput = findViewById(R.id.imageUrlInput);
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
        String name = itemName.getText().toString().trim();
        String description = itemDescription.getText().toString().trim();
        String price = itemPrice.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("description", description);
        item.put("price", Double.parseDouble(price));
        item.put("imageUrl", imageUrl);

        db.collection("items").add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddItemActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify HomeActivity of success
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
