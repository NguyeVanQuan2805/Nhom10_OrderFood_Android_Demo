package com.example.neworderfood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.activitis.InvoiceListActivity;
import com.example.neworderfood.activitis.LoginActivity;
import com.example.neworderfood.activitis.MenuActivity;
import com.example.neworderfood.activitis.PaymentActivity;
import com.example.neworderfood.activitis.TableStatusActivity;
import com.example.neworderfood.adapters.OrderAdapter;
import com.example.neworderfood.R;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.OrderDAO;
import com.example.neworderfood.models.Order;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.dao.OrderItemDAO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private DatabaseHelper dbHelper;
    private OrderDAO orderDAO;
    private List<Order> orders = new ArrayList<>();
    private SharedPreferences prefs;
    private  OrderItemDAO orderItemDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String role = prefs.getString("role", null);
        if (role == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        orderDAO = dbHelper.getOrderDAO();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Danh sách Order");
            }
        }

        recyclerView = findViewById(R.id.rv_orders);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView with ID rv_orders not found!");
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemDAO = dbHelper.getOrderItemDAO();  // Init DAO

// Callback mới
        adapter = new OrderAdapter(orders, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onPayClick(Order order) {
                // Load data và mở Payment (như hướng dẫn sửa trước)
                int tableNum = order.getTableNumber();
                List<OrderItem> items = orderItemDAO.getOrderItemsByOrderId(order.getId());
                if (tableNum > 0 && items != null && !items.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                    intent.putExtra("order_id", order.getId());
                    intent.putExtra("total_amount", order.getTotalAmount());
                    intent.putExtra("table_number", tableNum);
                    intent.putExtra("items", (Serializable) items);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi load data order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAddDishClick(Order order) {
                // Mở Menu để gọi thêm món (như hướng dẫn trước)
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.putExtra("isNewOrder", false);  // Gọi thêm
                intent.putExtra("orderId", order.getId());
                intent.putExtra("tableNumber", order.getTableNumber());
                intent.putExtra("totalAmount", order.getTotalAmount());
                startActivityForResult(intent, 3);  // Refresh sau khi thêm
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_add_order).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });

//        Button btnViewTables = findViewById(R.id.btn_view_tables);  // Thêm vào layout
//        btnViewTables.setOnClickListener(v -> {
//            Intent intent = new Intent(this, TableStatusActivity.class);
//            startActivity(intent);
//        });

        loadOrders();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();  // Cache ID to avoid repeated calls
        if (id == R.id.action_refresh) {
            loadOrders();  // Reload list
            Toast.makeText(this, "Đã làm mới", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            // TODO: Open settings activity
            Toast.makeText(this, "Cài đặt (coming soon)", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }else if (id == R.id.action_invoices) {  // THÊM: Mở list hóa đơn
            startActivity(new Intent(this, InvoiceListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK) {  // Từ thêm món
            loadOrders();  // Refresh list
        }
    }
    private void loadOrders() {
        if (orderDAO == null) {
            Log.e(TAG, "orderDAO is null!");
            return;
        }

        orders.clear();
        orders.addAll(orderDAO.getAllOrders());

        Log.d(TAG, "Loaded " + orders.size() + " orders.");

        if (recyclerView != null) {
            if (orders.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                findViewById(R.id.tv_empty).setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                findViewById(R.id.tv_empty).setVisibility(View.GONE);
            }
        }
    }
}