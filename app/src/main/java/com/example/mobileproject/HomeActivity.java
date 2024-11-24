package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity"; // For debug logs

    private RecyclerView recyclerView;
    private TextView emptyView;
    private Button logoutButton;
    private MarketplaceAdapter adapter;
    private List<MarketplaceItem> itemList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        logoutButton = findViewById(R.id.logoutButton);

        // Initialize Firestore and item list
        itemList = new ArrayList<>();
        adapter = new MarketplaceAdapter(this, itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Fetch items from Firestore
        fetchItemsFromFirebase();

        // Set up the Logout button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Logout button clicked."); // Debug log
                FirebaseAuth.getInstance().signOut(); // Logout from Firebase
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close current activity
                Log.d(TAG, "User logged out and redirected to LoginActivity."); // Debug log
            }
        });
    }

    private void fetchItemsFromFirebase() {
        Log.d(TAG, "Fetching items from Firestore..."); // Debug log
        db.collection("items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "Successfully fetched items from Firestore."); // Debug log
                        itemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MarketplaceItem item = document.toObject(MarketplaceItem.class);
                            itemList.add(item);
                            Log.d(TAG, "Item added: " + item.getName()); // Debug log
                        }
                        adapter.notifyDataSetChanged();

                        // Show or hide the empty view based on the items fetched
                        if (itemList.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            Log.d(TAG, "No items found. Displaying empty view."); // Debug log
                        } else {
                            emptyView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Items found. Displaying RecyclerView."); // Debug log
                        }
                    } else {
                        Log.e(TAG, "Error fetching items from Firestore.", task.getException()); // Debug log
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }
}
