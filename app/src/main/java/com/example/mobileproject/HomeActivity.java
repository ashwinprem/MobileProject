package com.example.mobileproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private androidx.appcompat.widget.SearchView searchView;
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
        searchView = findViewById(R.id.searchView);

        // Initialize Firestore and item list
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new MarketplaceAdapter(this, itemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch items from Firestore
        fetchItemsFromFirebase();

        // Set up search functionality
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void fetchItemsFromFirebase() {
        Log.d(TAG, "Fetching items from Firestore...");
        db.collection("items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        itemList.clear(); // Clear existing items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MarketplaceItem item = document.toObject(MarketplaceItem.class);
                            itemList.add(item); // Add fetched items to the list
                        }

                        adapter.filter(""); // Ensure the adapter is updated with all items initially
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
