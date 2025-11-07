package com.example.neworderfood.activitis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.adapters.DishAdapter;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.dao.OrderDAO;
import com.example.neworderfood.dao.OrderItemDAO;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.Order;
import com.example.neworderfood.models.OrderItem;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DishAdapter adapter;
    private DatabaseHelper dbHelper;
    private DishDAO dishDAO;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private int currentOrderId = 0;
    private int currentTable = 0;
    private List<OrderItem> orderItems = new ArrayList<>();
    private int totalAmount = 0;
    private List<Category> categories = new ArrayList<>();  // New: Dynamic categories

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        dishDAO = dbHelper.getDishDAO();
        orderDAO = dbHelper.getOrderDAO();
        orderItemDAO = dbHelper.getOrderItemDAO();

        setupToolbar();
        categories = dbHelper.getAllCategories();  // Load dynamic categories
        setupTabs();

        recyclerView = findViewById(R.id.rv_dishes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load first category if available
        if (!categories.isEmpty()) {
            loadDishes(categories.get(0).getId());
        }

        ImageButton btnTable = findViewById(R.id.btn_table);
        btnTable.setOnClickListener(v -> showTableDialog());

        ImageButton btnDiscount = findViewById(R.id.btn_discount);
        btnDiscount.setOnClickListener(v -> showDiscountDialog());

        ImageButton btnBill = findViewById(R.id.btn_bill);
        btnBill.setOnClickListener(v -> {
            if (orderItems.isEmpty()) {
                Toast.makeText(this, "Chưa chọn món", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, OrderSummaryActivity.class);
            intent.putExtra("table", currentTable);
            intent.putExtra("total", totalAmount);
            intent.putExtra("items", (java.io.Serializable) orderItems);
            startActivityForResult(intent, 1);
        });

        Button btnLuu = findViewById(R.id.btn_luu);
        btnLuu.setOnClickListener(v -> {
            if (currentTable == 0 || orderItems.isEmpty()) {
                Toast.makeText(this, "Cần chọn bàn và món", Toast.LENGTH_SHORT).show();
                return;
            }
            saveOrder();
        });

        Button btnThuTien = findViewById(R.id.btn_thu_tien);
        btnThuTien.setOnClickListener(v -> {
            if (orderItems.isEmpty()) {
                Toast.makeText(this, "Chưa có order để thu tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            Order tempOrder = new Order(0, currentTable, totalAmount);
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("order_id", 0);
            intent.putExtra("total_amount", totalAmount);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (orderItems.isEmpty()) {
                    finish();
                } else {
                    new AlertDialog.Builder(MenuActivity.this)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có muốn lưu order trước khi thoát?")
                            .setPositiveButton("Lưu và thoát", (dialog, which) -> {
                                if (currentTable == 0) {
                                    Toast.makeText(MenuActivity.this, "Chưa chọn bàn", Toast.LENGTH_SHORT).show();
                                } else {
                                    saveOrder();
                                }
                            })
                            .setNegativeButton("Thoát không lưu", (dialog, which) -> finish())
                            .setNeutralButton("Hủy", null)
                            .show();
                }
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chọn món");
        }
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_categories);
        for (Category cat : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(cat.getName()));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Category selectedCat = categories.get(tab.getPosition());
                loadDishes(selectedCat.getId());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        EditText etSearch = findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.getFilter().filter(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void loadDishes(int categoryId) {
        List<Dish> dishesList = dishDAO.getDishesByCategory(categoryId);
        if (dishesList == null) dishesList = new ArrayList<>();  // Null-safe
        Log.d("MenuActivity", "Loaded " + dishesList.size() + " dishes for category " + categoryId);

        adapter = new DishAdapter(dishesList, dish -> {
            Log.d("MenuActivity", "Dish clicked: " + dish.getName() + " (ID: " + dish.getId() + ")");  // ADD LOG ĐÂY
            Intent intent = new Intent(this, DishDetailActivity.class);
            intent.putExtra("dish", dish);
            startActivityForResult(intent, 2);
        });
        recyclerView.setAdapter(adapter);

        TextView tvTotal = findViewById(R.id.tv_total);
        tvTotal.setText(String.format("%,dđ", totalAmount));
    }

    private void showTableDialog() {
        String[] tables = {"Bàn 1", "Bàn 2", "Bàn 3", "Bàn 4", "Bàn 5"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn bàn")
                .setItems(tables, (dialog, which) -> {
                    currentTable = which + 1;
                    Toast.makeText(this, "Đã chọn Bàn " + currentTable, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showDiscountDialog() {
        EditText input = new EditText(this);
        input.setHint("Nhập % giảm (0-100)");
        new AlertDialog.Builder(this)
                .setTitle("Giảm giá (%)")
                .setView(input)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    String discountStr = input.getText().toString().trim();
                    if (discountStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập % giảm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int discount = Integer.parseInt(discountStr);
                        if (discount < 0 || discount > 100) {
                            Toast.makeText(this, "% giảm phải từ 0-100", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (totalAmount > 0) {
                            totalAmount = (int) (totalAmount * (100 - discount) / 100.0);
                            TextView tvTotal = findViewById(R.id.tv_total);
                            tvTotal.setText(String.format("%,dđ", totalAmount));
                            Toast.makeText(this, "Áp dụng giảm " + discount + "%", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Số không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveOrder() {
        Order order = new Order(0, currentTable, totalAmount);
        long orderIdLong = orderDAO.addOrder(order);
        if (orderIdLong > 0) {
            currentOrderId = (int) orderIdLong;
            for (OrderItem item : orderItems) {
                orderItemDAO.addOrderItem(item.copy(currentOrderId));
            }
            Toast.makeText(this, "Lưu thành công! Order ID: " + currentOrderId, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lỗi lưu order", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {  // From DishDetail
            OrderItem item = (OrderItem) data.getSerializableExtra("item");
            if (item != null) {
                orderItems.add(item);

                Dish dish = dishDAO.getDishById(item.getDishId());
                if (dish != null) {
                    int dishPrice = dish.getPrice();
                    int itemPrice = dishPrice * item.getQuantity();
                    if (item.getDiscount() > 0) {
                        itemPrice = (int) (itemPrice * (100 - item.getDiscount()) / 100.0);
                    }
                    totalAmount += itemPrice;
                } else {
                    Toast.makeText(this, "Không tìm thấy món ăn", Toast.LENGTH_SHORT).show();
                }

                TextView tvTotal = findViewById(R.id.tv_total);
                tvTotal.setText(String.format("%,dđ", totalAmount));
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}