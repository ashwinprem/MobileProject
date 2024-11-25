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

    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private Button logoutButton, addItemButton;
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
        addItemButton = findViewById(R.id.addItemButton);

        // Initialize Firestore and item list
        itemList = new ArrayList<>();
        adapter = new MarketplaceAdapter(this, itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Fetch items from Firestore
        fetchItemsFromFirebase();

        // Add Item button click listener
        addItemButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddItemActivity.class));
        });

        // Logout button click listener
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void fetchItemsFromFirebase() {
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                itemList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MarketplaceItem item = document.toObject(MarketplaceItem.class);
                    itemList.add(item);
                }
                adapter.filter(""); // Reset filter to show all items
                emptyView.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                Log.e(TAG, "Error fetching items from Firestore.", task.getException());
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}
