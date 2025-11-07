package com.example.neworderfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Dish;

import java.util.ArrayList;
import java.util.List;

public class AdminDishAdapter extends RecyclerView.Adapter<AdminDishAdapter.ViewHolder> {
    private List<Dish> dishes;
    private OnDishActionListener listener;

    public interface OnDishActionListener {
        void onDishAction(Dish dish);
    }

    public AdminDishAdapter(List<Dish> dishes, OnDishActionListener listener) {
        this.dishes = dishes != null ? dishes : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dish_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.nameText.setText(dish.getName());
        holder.priceText.setText(String.format("%,dđ", dish.getPrice()));  // Format

        // Có thể thêm category name nếu fetch

        // Load image
        if (dish.getImageResource() != 0) {
            holder.image.setImageResource(dish.getImageResource());
        } else {
            holder.image.setImageResource(R.drawable.bun_ca);  // Default
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDishAction(dish);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText;
        ImageView image;  // NEW: Add ImageView field

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.dish_name);
            priceText = view.findViewById(R.id.dish_price);
            image = view.findViewById(R.id.dish_image_admin);  // NEW: Initialize
        }
    }

    public void updateDishes(List<Dish> newDishes) {
        this.dishes = newDishes != null ? newDishes : new ArrayList<>();
        notifyDataSetChanged();
    }
}