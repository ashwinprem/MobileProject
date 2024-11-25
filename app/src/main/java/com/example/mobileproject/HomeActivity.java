package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private static final int ADD_ITEM_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private SearchView searchView;
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
        searchView = findViewById(R.id.searchView);
        logoutButton = findViewById(R.id.logoutButton);
        addItemButton = findViewById(R.id.addItemButton);

        // Initialize Firestore and item list
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new MarketplaceAdapter(this, itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchItemsFromFirebase();

        // Set up search filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query); // Filter items based on search query
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText); // Update filter as user types
                return false;
            }
        });

        // Add Item button click listener
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddItemActivity.class);
            startActivityForResult(intent, ADD_ITEM_REQUEST_CODE);
        });

        // Logout button click listener
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ITEM_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchItemsFromFirebase(); // Refresh items after adding a new one
        }
    }

    private void fetchItemsFromFirebase() {
        db.collection("items").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        itemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MarketplaceItem item = document.toObject(MarketplaceItem.class);
                            item.setId(document.getId()); // Set Firestore document ID
                            itemList.add(item);
                        }
                        adapter.filter(""); // Ensure adapter is updated with all items
                        emptyView.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        Log.e(TAG, "Error fetching items from Firestore", task.getException());
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
    }
}
