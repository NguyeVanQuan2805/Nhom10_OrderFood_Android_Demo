package com.example.neworderfood.adapters;

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

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {
    private List<OrderItem> items;
    private List<Dish> allDishes;  // Cache all dishes từ DAO
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

        // Fetch dish từ cache
        String dishName = "Món không xác định";
        int dishPrice = 0;
        if (allDishes != null) {
            for (Dish dish : allDishes) {
                if (dish.getId() == item.getDishId()) {
                    dishName = dish.getName();
                    dishPrice = dish.getPrice();
                    break;
                }
            }
        }

        holder.nameText.setText(dishName);
        holder.quantityText.setText("Số lượng: " + item.getQuantity());

        int itemTotal = dishPrice * item.getQuantity();
        if (item.getDiscount() > 0) {
            itemTotal = (int) (itemTotal * (100 - item.getDiscount()) / 100.0);
        }
        holder.priceText.setText(String.format("%,dđ", itemTotal));  // Format

        ImageView itemImage = holder.itemView.findViewById(R.id.item_image);  // Thêm ID
        if (itemImage != null) {
            Dish dish = allDishes.stream().filter(d -> d.getId() == item.getDishId()).findFirst().orElse(null);
            if (dish != null && dish.getImageResource() != 0) {
                itemImage.setImageResource(dish.getImageResource());
            } else {
                itemImage.setImageResource(R.drawable.bun_ca);
            }
        }
        if (item.getNotes() != null && !item.getNotes().isEmpty()) {
            holder.notesText.setText("Ghi chú: " + item.getNotes());
            holder.notesText.setVisibility(View.VISIBLE);
        } else {
            holder.notesText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText, priceText, notesText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.item_name);
            quantityText = view.findViewById(R.id.item_quantity);
            priceText = view.findViewById(R.id.item_price);
            notesText = view.findViewById(R.id.item_notes);
        }
    }

    // Update items mới
    public void updateItems(List<OrderItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Refresh dishes cache nếu DB thay đổi
    public void refreshDishes() {
        loadAllDishes();
        notifyDataSetChanged();
    }
}