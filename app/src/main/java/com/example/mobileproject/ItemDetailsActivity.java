package com.example.mobileproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemName, itemDescription, itemPrice;
    private ImageView itemImage;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Initialize views
        itemName = findViewById(R.id.itemName);
        itemDescription = findViewById(R.id.itemDescription);
        itemPrice = findViewById(R.id.itemPrice);
        itemImage = findViewById(R.id.itemImage);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the Item ID passed from MarketplaceAdapter
        String itemId = getIntent().getStringExtra("ITEM_ID");

        if (itemId != null) {
            fetchItemDetails(itemId);
        } else {
            Toast.makeText(this, "Error: Item ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchItemDetails(String itemId) {
        db.collection("items").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Map document to MarketplaceItem
                        MarketplaceItem item = documentSnapshot.toObject(MarketplaceItem.class);
                        if (item != null) {
                            // Update UI with item details
                            itemName.setText(item.getName());
                            itemDescription.setText(item.getDescription());
                            itemPrice.setText("$" + item.getPrice());
                            Glide.with(this).load(item.getImageUrl()).into(itemImage);
                        }
                    } else {
                        Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching item details", Toast.LENGTH_SHORT).show();
                });
    }
}
