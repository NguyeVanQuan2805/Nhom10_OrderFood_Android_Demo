package com.example.neworderfood.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> implements Filterable {
    private List<Dish> dishes;
    private List<Dish> originalDishes;
    private OnDishClickListener listener;

    public interface OnDishClickListener {
        void onDishClick(Dish dish);
    }

    public DishAdapter(List<Dish> dishes, OnDishClickListener listener) {
        this.dishes = dishes != null ? dishes : new ArrayList<>();
        this.originalDishes = new ArrayList<>(this.dishes);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.nameText.setText(dish.getName());
        holder.priceText.setText(String.format("%,dđ", dish.getPrice()));

        // HIỂN THỊ ẢNH - ƯU TIÊN ẢNH CUSTOM
        loadDishImage(holder.image, dish);

        holder.itemView.setOnClickListener(v -> {
            Log.d("DishAdapter", "Item clicked: " + dish.getName());  // ADD LOG TO DEBUG
            if (listener != null) {
                listener.onDishClick(dish);
            }
        });

        // OPTIONAL: Make subviews non-focusable to avoid blocking
        holder.nameText.setFocusable(false);
        holder.priceText.setFocusable(false);
        holder.image.setFocusable(false);
    }

    private void loadDishImage(ImageView imageView, Dish dish) {
        // Ưu tiên custom Base64
        if (dish.hasCustomImage()) {
            Bitmap customBitmap = ImageUtils.base64ToBitmap(dish.getImageBase64());
            if (customBitmap != null) {
                imageView.setImageBitmap(customBitmap);
                Log.d("DishAdapter", "Loaded custom image for " + dish.getName());
                return;
            } else {
                Log.w("DishAdapter", "Invalid Base64 for " + dish.getName() + " - fallback to resource");
            }
        }

        // Fallback resource (nếu >0)
        if (dish.getImageResource() > 0) {
            imageView.setImageResource(dish.getImageResource());
            Log.d("DishAdapter", "Loaded resource image for " + dish.getName());
            return;
        }

        // Default
        imageView.setImageResource(R.drawable.bun_ca);
        Log.d("DishAdapter", "Used default image for " + dish.getName());
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText;
        ImageView image;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.dish_name);
            priceText = view.findViewById(R.id.dish_price);
            image = view.findViewById(R.id.dish_image);
        }
    }

    // Filter cho search
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = originalDishes;
                } else {
                    List<Dish> filtered = new ArrayList<>();
                    String query = constraint.toString().toLowerCase().trim();
                    for (Dish dish : originalDishes) {
                        if (dish.getName().toLowerCase().contains(query)) {
                            filtered.add(dish);
                        }
                    }
                    results.values = filtered;
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dishes.clear();
                dishes.addAll((List<Dish>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    // Update list mới
    public void updateDishes(List<Dish> newDishes) {
        this.dishes = newDishes != null ? newDishes : new ArrayList<>();
        this.originalDishes = new ArrayList<>(this.dishes);
        notifyDataSetChanged();
    }
}