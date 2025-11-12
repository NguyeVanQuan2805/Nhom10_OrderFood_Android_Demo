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
    private OnOrderActionListener listener;  // SỬA: Đổi tên interface để hỗ trợ nhiều action

    // SỬA: Interface hỗ trợ cả "Thu tiền" và "Thêm món"
    public interface OnOrderActionListener {
        void onPayClick(Order order);
        void onAddDishClick(Order order);  // THÊM: Callback cho thêm món
    }

    public OrderAdapter(List<Order> orders, OnOrderActionListener listener) {
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
        holder.tableText.setText("Bàn " + order.getTableNumber());  // SỬA: Dùng getTableNumber() (đảm bảo model có method này)
        holder.totalText.setText(String.format("%,dđ", order.getTotalAmount()));  // Format số tiền
        holder.statusText.setText("Trạng thái: " + order.getStatus());  // Nếu có status view
        holder.payButton.setOnClickListener(v -> listener.onPayClick(order));  // Thu tiền

        // THÊM: Button "Thêm món" cho order đang phục vụ
        if ("Pending".equals(order.getStatus()) || "đang phục vụ".equals(order.getStatus())) {  // Chỉ hiện nếu chưa paid
            holder.addDishButton.setVisibility(View.VISIBLE);
            holder.addDishButton.setOnClickListener(v -> listener.onAddDishClick(order));
        } else {
            holder.addDishButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tableText, totalText, statusText;
        Button payButton, addDishButton;  // THÊM: Button thêm món

        ViewHolder(View view) {
            super(view);
            tableText = view.findViewById(R.id.order_table);
            totalText = view.findViewById(R.id.order_total);
            statusText = view.findViewById(R.id.order_status);  // Thêm nếu có trong layout
            payButton = view.findViewById(R.id.btn_thu_tien);
            addDishButton = view.findViewById(R.id.btn_them_mon);  // THÊM: ID button mới
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