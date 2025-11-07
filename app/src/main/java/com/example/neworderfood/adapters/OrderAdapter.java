package com.example.neworderfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orders;  // List hiển thị hiện tại
    private List<Order> originalOrders;  // List gốc để filter
    private OnPayClickListener listener;

    public interface OnPayClickListener {
        void onPayClick(Order order);
    }

    public OrderAdapter(List<Order> orders, OnPayClickListener listener) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.originalOrders = new ArrayList<>(this.orders);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tableText.setText("Bàn " + order.getTableNumber());
        holder.totalText.setText(String.format("%,dđ", order.getTotalAmount()));  // Format số tiền
        holder.statusText.setText("Trạng thái: " + order.getStatus());  // Nếu có status view
        holder.payButton.setOnClickListener(v -> listener.onPayClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tableText, totalText, statusText;
        Button payButton;

        ViewHolder(View view) {
            super(view);
            tableText = view.findViewById(R.id.order_table);
            totalText = view.findViewById(R.id.order_total);
            statusText = view.findViewById(R.id.order_status);  // Thêm nếu có trong layout
            payButton = view.findViewById(R.id.btn_thu_tien);
        }
    }

    // Update list mới (sử dụng trong MainActivity khi reload)
    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders != null ? newOrders : new ArrayList<>();
        this.originalOrders = new ArrayList<>(this.orders);
        notifyDataSetChanged();
    }

    // Optional: Filter (ví dụ: pending orders)
    public void filterOrders(String query) {
        if (query.isEmpty()) {
            orders.clear();
            orders.addAll(originalOrders);
        } else {
            List<Order> filtered = new ArrayList<>();
            for (Order order : originalOrders) {
                if (String.valueOf(order.getTableNumber()).contains(query) ||
                        order.getStatus().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(order);
                }
            }
            orders.clear();
            orders.addAll(filtered);
        }
        notifyDataSetChanged();
    }
}