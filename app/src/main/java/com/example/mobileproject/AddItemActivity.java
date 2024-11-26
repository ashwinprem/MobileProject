package com.example.mobileproject;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import org.json.JSONObject;
import android.os.StrictMode;

public class AddItemActivity extends AppCompatActivity {

    private EditText itemName, itemDescription, itemPrice, imageUrlInput;
    private Button submitButton;

    private FirebaseFirestore db;

    // Declare latitude and longitude as class-level variables
    private double latitude;
    private double longitude;

    private FusedLocationProviderClient fusedLocationClient;

    String apiKey = "";

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

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> addItemToFirestore());

        // Enable back button in Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Request location if permissions are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    // This function gets the user's location and stores it in class-level variables
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Create a location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Use high accuracy
                .setInterval(10000) // Interval in milliseconds for active location updates (not used here)
                .setFastestInterval(5000) // Fastest interval in milliseconds (not used here)
                .setNumUpdates(1); // Get only one update

        // Define a LocationCallback
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(AddItemActivity.this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the most recent location
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(AddItemActivity.this,
                            "Location: Lat: " + latitude + ", Long: " + longitude,
                            Toast.LENGTH_SHORT).show();

                    // Optionally fetch the address
                    new Thread(() -> {
                        String result = getAddressFromLatLong(latitude, longitude);
                        runOnUiThread(() -> {
                            if (result != null) {
                                Log.d("addressGenerated", result);
                            } else {
                                Toast.makeText(AddItemActivity.this, "Failed to get address", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                }
            }
        };

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
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

        // Store latitude and longitude in Firestore (if available)
        if (latitude != 0 && longitude != 0) {
            item.put("latitude", latitude);
            item.put("longitude", longitude);
        }

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

    private String getAddressFromLatLong(double latitude, double longitude) {
        String address = null;

        try {
            // Build the URL for the geocoding request
            String urlString = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                    latitude, longitude, apiKey
            );

            // Create a URL object and open a connection
            URL url = new URL(urlString);
            Log.d("URLCrafted", urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Log the raw response to help debug
            Log.d("GeocodingResponse", response.toString());

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());

            // Check if the status is OK
            if (jsonResponse.getString("status").equals("OK")) {
                // Get the formatted address from the response
                address = jsonResponse.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("formatted_address");
            } else {
                // Handle any status other than OK
                address = "Address not found (status: " + jsonResponse.getString("status") + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            address = "Error: " + e.getMessage();
        }

        return address;
    }
}