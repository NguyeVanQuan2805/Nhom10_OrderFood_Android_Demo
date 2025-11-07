package com.example.neworderfood.adapters;

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

import java.util.ArrayList;
import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder> implements Filterable {
    private List<Dish> dishes;  // List hiển thị hiện tại
    private List<Dish> originalDishes;  // List gốc để filter
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
        holder.priceText.setText(String.format("%,dđ", dish.getPrice()));  // Format số tiền
        // Load image if needed (placeholder)
        if (dish.getImageResource() != 0) {
            holder.image.setImageResource(dish.getImageResource());
        } else {
            holder.image.setImageResource(R.drawable.bun_ca);  // Default
        }
        holder.itemView.setOnClickListener(v -> listener.onDishClick(dish));
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

    // Filter cho search (tìm theo name hoặc category, nhưng vì categoryId, có thể extend nếu cần name)
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
                        if (dish.getName().toLowerCase().contains(query)) {  // Có thể thêm category name nếu fetch
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

    // Update list mới (cho load category khác)
    public void updateDishes(List<Dish> newDishes) {
        this.dishes = newDishes != null ? newDishes : new ArrayList<>();
        this.originalDishes = new ArrayList<>(this.dishes);
        notifyDataSetChanged();
    }
}