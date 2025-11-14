package com.example.neworderfood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.activities.InvoiceListActivity;
import com.example.neworderfood.activities.LoginActivity;
import com.example.neworderfood.activities.MenuActivity;
import com.example.neworderfood.activities.PaymentActivity;
import com.example.neworderfood.adapters.OrderAdapter;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.OrderDao;
import com.example.neworderfood.room.daos.OrderItemDao;
import com.example.neworderfood.models.Order;
import com.example.neworderfood.room.entities.OrderEntity;
import com.example.neworderfood.room.entities.OrderItemEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ADD_DISH_REQUEST = 3;

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private AppDatabase db;
    private OrderDao orderDAO;
    private OrderItemDao orderItemDAO;
    private List<Order> orders = new ArrayList<>();
    private SharedPreferences prefs;

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

        db = AppDatabase.getInstance(this);
        orderDAO = db.orderDao();
        orderItemDAO = db.orderItemDao();

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

        // FIXED: Init adapter with FULL listener implementation (both methods)
        adapter = new OrderAdapter(new ArrayList<>(), new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onPayClick(Order order) {
                // FIXED: Async load items to avoid UI block
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    List<OrderItemEntity> entityItems = orderItemDAO.getOrderItemsByOrderId(order.getId());
                    List<OrderItem> items = OrderItem.fromEntities(entityItems);
                    runOnUiThread(() -> {
                        int tableNum = order.getTableNumber();
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
                    });
                });
                executor.shutdown();
            }

            @Override
            public void onAddDishClick(Order order) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.putExtra("isNewOrder", false);  // Gọi thêm món
                intent.putExtra("orderId", order.getId());
                intent.putExtra("tableNumber", order.getTableNumber());
                intent.putExtra("totalAmount", order.getTotalAmount());
                startActivityForResult(intent, ADD_DISH_REQUEST);  // Refresh sau khi thêm
            }
        });
        recyclerView.setAdapter(adapter);  // FIXED: Set adapter early to avoid "no adapter" warning

        findViewById(R.id.btn_add_order).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });

        loadOrders();  // FIXED: Call async load
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
        } else if (id == R.id.action_invoices) {  // THÊM: Mở list hóa đơn
            startActivity(new Intent(this, InvoiceListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();  // FIXED: Reload khi resume
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_DISH_REQUEST && resultCode == RESULT_OK) { // Từ thêm món
            loadOrders(); // Refresh list
        } else if (resultCode == RESULT_OK) {  // Từ Payment (không requestCode cụ thể)
            loadOrders();  // Reload sau paid để remove order
        }
    }

    // FIXED: Async load to prevent main thread overload (skipped frames)
    private void loadOrders() {
        if (orderDAO == null) {
            Log.e(TAG, "orderDAO is null!");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            orders.clear();
            List<OrderEntity> entities = orderDAO.getAllOrders();
            if (entities != null) {
                orders.addAll(Order.fromEntities(entities));
            }

            Log.d(TAG, "Loaded " + orders.size() + " orders.");

            runOnUiThread(() -> {
                // FIXED: Update adapter & visibility on UI thread
                if (orders.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    findViewById(R.id.tv_empty).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.tv_empty).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                // FIXED: Use updateOrders() to refresh data without recreating adapter
                adapter.updateOrders(orders);
                recyclerView.setAdapter(adapter);  // Ensure set (harmless if already set)
            });
        });
        executor.shutdown();
    }
}