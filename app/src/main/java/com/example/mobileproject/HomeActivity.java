package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Initialize Firebase and data structures
        db = FirebaseFirestore.getInstance();
        itemList = new ArrayList<>();
        adapter = new MarketplaceAdapter(this, itemList);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch items from Firestore with real-time updates
        listenForFirestoreUpdates();

        // Set up Logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        // Set up Add Item button
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddItemActivity.class);
            startActivity(intent);
        });
    }

    private void listenForFirestoreUpdates() {
        db.collection("items").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Error listening for Firestore updates", e);
                return;
            }

            if (snapshots != null) {
                for (DocumentChange change : snapshots.getDocumentChanges()) {
                    switch (change.getType()) {
                        case ADDED:
                            MarketplaceItem newItem = change.getDocument().toObject(MarketplaceItem.class);
                            newItem.setDocumentId(change.getDocument().getId());
                            itemList.add(newItem);
                            adapter.notifyItemInserted(itemList.size() - 1);
                            break;
                        case MODIFIED:
                            MarketplaceItem updatedItem = change.getDocument().toObject(MarketplaceItem.class);
                            updatedItem.setDocumentId(change.getDocument().getId());
                            int indexToUpdate = findItemIndex(change.getDocument().getId());
                            if (indexToUpdate != -1) {
                                itemList.set(indexToUpdate, updatedItem);
                                adapter.notifyItemChanged(indexToUpdate);
                            }
                            break;
                        case REMOVED:
                            int indexToRemove = findItemIndex(change.getDocument().getId());
                            if (indexToRemove != -1) {
                                itemList.remove(indexToRemove);
                                adapter.notifyItemRemoved(indexToRemove);
                            }
                            break;
                    }
                }
                toggleEmptyView();
            }
        });
    }

    private void toggleEmptyView() {
        if (itemList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private int findItemIndex(String documentId) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getDocumentId().equals(documentId)) {
                return i;
            }
        }
        return -1;
    }
}
