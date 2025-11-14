package com.example.neworderfood.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.utils.ImageUtils;

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
        holder.priceText.setText(String.format("%,dđ", dish.getPrice()));

        // HIỂN THỊ ẢNH - ƯU TIÊN ẢNH CUSTOM
        loadDishImage(holder.image, dish);

        // Hiển thị category name nếu có
        if (holder.categoryText != null) {
            // Có thể thêm logic để hiển thị tên category nếu cần
            holder.categoryText.setText("Món ăn");
        }

        // Hiển thị indicator ảnh custom
        showCustomImageIndicator(holder, dish);

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDishAction(dish);
            }
            return true;
        });
    }

    /**
     * Phương thức load ảnh cho món ăn trong admin
     */
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

    /**
     * Hiển thị indicator cho ảnh custom (tuỳ chọn)
     */
    private void showCustomImageIndicator(ViewHolder holder, Dish dish) {
        if (dish.hasCustomImage()) {
            // Có thể thêm badge hoặc indicator để biết đây là ảnh custom
            // Ví dụ: đổi màu border hoặc thêm icon nhỏ
            holder.image.setBackgroundResource(R.drawable.image_border_custom);
        } else {
            holder.image.setBackgroundResource(R.drawable.image_border);
        }
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, priceText, categoryText;
        ImageView image;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.dish_name);
            priceText = view.findViewById(R.id.dish_price);
            image = view.findViewById(R.id.dish_image_admin);

            // Category text có thể không có trong layout, kiểm tra trước
            categoryText = view.findViewById(R.id.dish_category);
        }
    }

    public void updateDishes(List<Dish> newDishes) {
        this.dishes = newDishes != null ? newDishes : new ArrayList<>();
        notifyDataSetChanged();
    }
}