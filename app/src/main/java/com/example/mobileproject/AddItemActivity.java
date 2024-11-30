package com.example.mobileproject;

import android.annotation.SuppressLint;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

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

import org.json.JSONArray;
import org.json.JSONObject;
import android.os.StrictMode;

public class AddItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText itemName, itemDescription, itemPrice, imageUrlInput, postalCodeInput;
    private Button submitButton;

    private FirebaseFirestore db;

    // Declare latitude and longitude as class-level variables
    private double latitude;
    private double longitude;

    private FusedLocationProviderClient fusedLocationClient;

    String apiKey = "REPLACE_WITH_KEY";

    private GoogleMap mMap;

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
            postalCodeInput = findViewById(R.id.postalCodeInput);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> addItemToFirestore());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

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

    // This function gets the user's lat and long
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000)
                .setNumUpdates(1);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(AddItemActivity.this,
                            "Failed to get current location", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    // Update map with current location
                    if (mMap != null) {
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("Current Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    }
                    fetchAndSetPostalCode(latitude,longitude);
                }
            }
        };

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


        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        item.put("location", geoPoint);

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable the my-location layer if permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        getUserLocation();
    }

    private void getPostalCodeFromLatLong(double latitude,
                                          double longitude,
                                          PostalCodeCallback callback) {
        new Thread(() -> {
            String postalCode = null;
            try {
                @SuppressLint("DefaultLocale") String urlString = String.format(
                        "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                        latitude, longitude, apiKey
                );
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getString("status").equals("OK")) {
                    JSONArray results = jsonResponse.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        JSONArray addressComponents = result.getJSONArray("address_components");
                        for (int j = 0; j < addressComponents.length(); j++) {
                            JSONObject component = addressComponents.getJSONObject(j);
                            JSONArray types = component.getJSONArray("types");
                            if (types.toString().contains("postal_code")) {
                                postalCode = component.getString("long_name");
                                break;
                            }
                        }
                        if (postalCode != null) break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalPostalCode = postalCode;
            runOnUiThread(() ->
                    callback.onPostalCodeFetched(finalPostalCode));
        }).start();
    }

    private void fetchAndSetPostalCode(double latitude, double longitude) {
        EditText postalCodeInput = findViewById(R.id.postalCodeInput);
        getPostalCodeFromLatLong(latitude, longitude, postalCode -> {
            if (postalCode != null) {
                postalCodeInput.setText(postalCode);
                //Toast.makeText(this, "Postal Code Auto Populated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to fetch postal code", Toast.LENGTH_SHORT).show();
            }
        });
    }



}