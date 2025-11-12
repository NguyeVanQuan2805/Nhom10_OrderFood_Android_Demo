package com.example.neworderfood.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private List<OrderItem> items;
    private List<Dish> allDishes;
    private DishDAO dishDAO;

    public OrderItemAdapter(List<OrderItem> items, DishDAO dishDAO) {
        this.items = items != null ? items : new ArrayList<>();
        this.dishDAO = dishDAO;
        loadAllDishes();
    }

    private void loadAllDishes() {
        allDishes = dishDAO != null ? dishDAO.getAllDishes() : new ArrayList<>();
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

        // Tìm món ăn tương ứng
        Dish dish = findDishById(item.getDishId());

        if (dish != null) {
            // Hiển thị thông tin món ăn
            holder.nameText.setText(dish.getName());
            holder.quantityText.setText("Số lượng: " + item.getQuantity());

            // Tính tổng tiền cho item
            int itemTotal = calculateItemTotal(dish, item);
            holder.priceText.setText(String.format("%,dđ", itemTotal));

            // HIỂN THỊ ẢNH MÓN ĂN
            loadDishImage(holder.itemImage, dish);

            // Hiển thị ghi chú nếu có
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                holder.notesText.setText("Ghi chú: " + item.getNotes());
                holder.notesText.setVisibility(View.VISIBLE);
            } else {
                holder.notesText.setVisibility(View.GONE);
            }
        } else {
            // Fallback nếu không tìm thấy món ăn
            holder.nameText.setText("Món không xác định");
            holder.quantityText.setText("Số lượng: " + item.getQuantity());
            holder.priceText.setText("0đ");
            holder.notesText.setVisibility(View.GONE);
            holder.itemImage.setImageResource(R.drawable.bun_ca);
        }
    }

    /**
     * Tìm món ăn theo ID
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
     * Tính tổng tiền cho item (có tính discount)
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
     * Load ảnh cho món ăn trong order item
     */
    private void loadDishImage(ImageView imageView, Dish dish) {
        // Ưu tiên ảnh custom Base64
        if (dish.hasCustomImage()) {
            Bitmap customBitmap = ImageUtils.base64ToBitmap(dish.getImageBase64());
            if (customBitmap != null) {
                imageView.setImageBitmap(customBitmap);
                return;
            }
        }

        // Fallback đến ảnh resource
        if (dish.getImageResource() != 0) {
            try {
                imageView.setImageResource(dish.getImageResource());
                return;
            } catch (Exception e) {
                // Resource không tồn tại
            }
        }

        // Ảnh mặc định
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

    public void refreshDishes() {
        loadAllDishes();
        notifyDataSetChanged();
    }
}