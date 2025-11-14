package com.example.neworderfood.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.utils.ImageUtils;
import com.example.neworderfood.room.entities.DishEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private static final String TAG = "OrderItemAdapter";
    private List<OrderItem> items;
    private List<Dish> allDishes = new ArrayList<>();  // FIXED: Initialize empty to avoid null
    private DishDao dishDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());  // FIXED: Use Handler for UI updates

    public OrderItemAdapter(List<OrderItem> items, DishDao dishDao) {
        this.items = items != null ? items : new ArrayList<>();
        this.dishDao = dishDao;
        loadAllDishesAsync();  // FIXED: Load async to avoid main thread block
    }

    // FIXED: Load allDishes async with sync query to ensure data ready
    private void loadAllDishesAsync() {
        if (dishDao != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                // FIXED: Use sync query instead of LiveData.getValue() (which may be null if not observed)
                List<DishEntity> entities = dishDao.getAllDishesSync();  // Assume DAO has sync method
                if (entities == null) {
                    entities = new ArrayList<>();
                }
                allDishes = Dish.fromEntities(entities);
                Log.d(TAG, "Loaded " + allDishes.size() + " dishes for adapter");
                // FIXED: Post to main thread instead of runOnUiThread (no Activity context)
                mainHandler.post(() -> notifyDataSetChanged());  // Refresh UI if needed
            });
            executor.shutdown();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        // FIXED: Find dish - fallback if not loaded yet
        Dish dish = findDishById(item.getDishId());
        if (dish != null) {
            // Display dish info
            holder.nameText.setText(dish.getName());
            holder.quantityText.setText("Số lượng: " + item.getQuantity());
            // Calculate item total
            int itemTotal = calculateItemTotal(dish, item);
            holder.priceText.setText(String.format("%,dđ", itemTotal));
            // Load dish image
            loadDishImage(holder.itemImage, dish);
            // Display notes if any
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                holder.notesText.setText("Ghi chú: " + item.getNotes());
                holder.notesText.setVisibility(View.VISIBLE);
            } else {
                holder.notesText.setVisibility(View.GONE);
            }
        } else {
            // FIXED: Better fallback with loading indicator or log
            holder.nameText.setText("Món không xác định (ID: " + item.getDishId() + ")");
            holder.quantityText.setText("Số lượng: " + item.getQuantity());
            holder.priceText.setText("0đ");
            holder.notesText.setVisibility(View.GONE);
            holder.itemImage.setImageResource(R.drawable.bun_ca);
            Log.w(TAG, "Dish not found for ID: " + item.getDishId() + " - Check DB or load timing");
        }
    }

    /**
     * Find dish by ID
     */
    private Dish findDishById(int dishId) {
        for (Dish dish : allDishes) {
            if (dish.getId() == dishId) {
                return dish;
            }
        }
        return null;
    }

    /**
     * Calculate item total (with discount)
     */
    private int calculateItemTotal(Dish dish, OrderItem item) {
        int dishPrice = dish.getPrice();
        int itemPrice = dishPrice * item.getQuantity();
        if (item.getDiscount() > 0) {
            itemPrice = (int) (itemPrice * (100 - item.getDiscount()) / 100.0);
        }
        return itemPrice;
    }

    /**
     * Load image for dish in order item
     */
    private void loadDishImage(ImageView imageView, Dish dish) {
        // Prioritize custom Base64 image
        if (dish.hasCustomImage()) {
            Bitmap customBitmap = ImageUtils.base64ToBitmap(dish.getImageBase64());
            if (customBitmap != null) {
                imageView.setImageBitmap(customBitmap);
                return;
            }
        }
        // Fallback to resource image
        if (dish.getImageResource() != 0) {
            try {
                imageView.setImageResource(dish.getImageResource());
                return;
            } catch (Exception e) {
                // Resource not exist
            }
        }
        // Default image
        imageView.setImageResource(R.drawable.bun_ca);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText, priceText, notesText;
        ImageView itemImage;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.item_name);
            quantityText = view.findViewById(R.id.item_quantity);
            priceText = view.findViewById(R.id.item_price);
            notesText = view.findViewById(R.id.item_notes);
            itemImage = view.findViewById(R.id.item_image);
        }
    }

    public void updateItems(List<OrderItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    // FIXED: Public refresh to reload dishes if needed (call from activity)
    public void refreshDishes() {
        loadAllDishesAsync();
    }
}