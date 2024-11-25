package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private Context context;
    private List<MarketplaceItem> originalItemList;
    private List<MarketplaceItem> filteredItemList;

    public MarketplaceAdapter(Context context, List<MarketplaceItem> itemList) {
        this.context = context;
        this.originalItemList = itemList;
        this.filteredItemList = new ArrayList<>(itemList); // Make a copy for filtering
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_marketplace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketplaceItem item = filteredItemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemDescription.setText(item.getDescription());
        holder.itemPrice.setText("$" + item.getPrice());

        // Load image using Glide
        Glide.with(context).load(item.getImageUrl()).into(holder.itemImage);

        // Add click listener for item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemDetailsActivity.class);
            intent.putExtra("ITEM_ID", item.getId()); // Pass the item's ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredItemList.size();
    }

    public void filter(String query) {
        filteredItemList.clear();
        if (query.isEmpty()) {
            filteredItemList.addAll(originalItemList);
        } else {
            for (MarketplaceItem item : originalItemList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredItemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemDescription, itemPrice;
        ImageView itemImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemImage = itemView.findViewById(R.id.itemImage);
        }
    }
}
