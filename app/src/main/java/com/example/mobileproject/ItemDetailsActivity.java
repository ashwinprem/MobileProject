package com.example.mobileproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemName, itemPrice, itemDescription;
    private ImageView itemImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        // Initialize views
        itemName = findViewById(R.id.itemName);
        itemPrice = findViewById(R.id.itemPrice);
        itemDescription = findViewById(R.id.itemDescription);
        itemImage = findViewById(R.id.itemImage);

        // Get the data passed from the HomeActivity
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String price = getIntent().getStringExtra("price");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Populate views with item data
        itemName.setText(name);
        itemPrice.setText("$" + price);
        itemDescription.setText(description);

        // Load image using Glide
        Glide.with(this).load(imageUrl).into(itemImage);

        // Enable back button in Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
