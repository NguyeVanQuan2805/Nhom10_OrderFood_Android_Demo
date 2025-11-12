package com.example.neworderfood.activitis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.example.neworderfood.models.Table;

import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private static final String TAG = "MenuActivity";
    private static final int ADD_DISH_REQUEST = 3;  // Để onActivityResult từ MainActivity

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
    private List<Category> categories = new ArrayList<>();
    private EditText etSearch;
    private boolean isNewOrder = true;  // Default: order mới

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

        // KHỞI TẠO RecyclerView TRƯỚC
        recyclerView = findViewById(R.id.rv_dishes);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView not found! Check layout XML");
            Toast.makeText(this, "Lỗi khởi tạo giao diện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        dishDAO = dbHelper.getDishDAO();
        orderDAO = dbHelper.getOrderDAO();
        orderItemDAO = dbHelper.getOrderItemDAO();

        // XỬ LÝ INTENT: Phân biệt order mới hoặc gọi thêm món
        Intent receivedIntent = getIntent();
        isNewOrder = receivedIntent.getBooleanExtra("isNewOrder", true);  // Default: order mới
        int passedOrderId = receivedIntent.getIntExtra("orderId", 0);
        int passedTable = receivedIntent.getIntExtra("tableNumber", 0);
        int passedTotal = receivedIntent.getIntExtra("totalAmount", 0);

        if (!isNewOrder && passedOrderId > 0) {
            // Gọi thêm món: Load order hiện tại
            currentOrderId = passedOrderId;
            currentTable = passedTable;
            totalAmount = passedTotal;
            orderItems = orderItemDAO.getOrderItemsByOrderId(currentOrderId);  // Load items từ DB
            if (orderItems != null) {
                // Update total từ items (tính lại nếu cần)
                for (OrderItem item : orderItems) {
                    Dish dish = dishDAO.getDishById(item.getDishId());
                    if (dish != null) totalAmount += dish.getPrice() * item.getQuantity();
                }
            }
            TextView tvTotal = findViewById(R.id.tv_total);
            tvTotal.setText(String.format("%,dđ", totalAmount));
            // Ẩn nút chọn bàn (đã có bàn)
            findViewById(R.id.btn_table).setVisibility(View.GONE);
            Toast.makeText(this, "Đang gọi thêm món cho Bàn " + currentTable, Toast.LENGTH_SHORT).show();
        } else {
            // Order mới: Giữ nguyên, currentTable = 0
        }

        setupToolbar();
        categories = dbHelper.getAllCategories();
        setupTabs();
        setupSearch();

        ImageButton btnTable = findViewById(R.id.btn_table);
        btnTable.setOnClickListener(v -> showTableDialog(isNewOrder));  // Pass isNewOrder để filter

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
            intent.putExtra("items", (Serializable) orderItems);
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
            if (currentTable == 0 || orderItems.isEmpty()) {
                Toast.makeText(this, "Cần chọn bàn và món", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lưu order trước khi thu tiền
            saveOrder();  // Lưu/update order
            if (currentOrderId > 0) {
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("order_id", currentOrderId);
                intent.putExtra("total_amount", totalAmount);
                intent.putExtra("table_number", currentTable);
                intent.putExtra("items", (Serializable) orderItems);
                startActivity(intent);
            }
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

    private void setupSearch() {
        etSearch = findViewById(R.id.et_search);
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

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_categories);
        if (tabLayout == null) {
            Log.e(TAG, "TabLayout not found!");
            return;
        }

        // Clear existing tabs
        tabLayout.removeAllTabs();

        // Add tabs from categories
        for (Category cat : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(cat.getName()));
        }

        // Load dishes for first category if available
        if (!categories.isEmpty()) {
            loadDishes(categories.get(0).getId());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position >= 0 && position < categories.size()) {
                    Category selectedCat = categories.get(position);
                    loadDishes(selectedCat.getId());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadDishes(int categoryId) {
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is null in loadDishes!");
            return;
        }

        List<Dish> dishesList = dishDAO.getDishesByCategory(categoryId);
        if (dishesList == null) {
            dishesList = new ArrayList<>();
        }
        Log.d(TAG, "Loaded " + dishesList.size() + " dishes for category " + categoryId);

        adapter = new DishAdapter(dishesList, dish -> {
            Log.d(TAG, "Dish clicked: " + dish.getName() + ", categoryId: " + categoryId);
            Intent intent = new Intent(this, com.example.neworderfood.activitis.DishDetailActivity.class);
            intent.putExtra("dish", dish);
            intent.putExtra("categoryId", categoryId);
            startActivityForResult(intent, 2);
        });

        // QUAN TRỌNG: Set adapter cho RecyclerView
        recyclerView.setAdapter(adapter);

        // Update total
        TextView tvTotal = findViewById(R.id.tv_total);
        if (tvTotal != null) {
            tvTotal.setText(String.format("%,dđ", totalAmount));
        }
    }

    private void showTableDialog(boolean onlyEmpty) {
        // Load tables từ DB
        List<Table> tablesList = dbHelper.getAllTables();
        if (tablesList.isEmpty()) {
            Toast.makeText(this, "Chưa có bàn nào. Vui lòng thêm bàn ở phần admin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filter nếu order mới (onlyEmpty = true)
        List<Table> filteredTables = new ArrayList<>();
        for (Table table : tablesList) {
            if (!onlyEmpty || "Còn trống".equals(table.getStatus())) {
                filteredTables.add(table);
            }
        }
        if (filteredTables.isEmpty()) {
            Toast.makeText(this, "Không còn bàn trống để order mới!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo array string từ filtered list
        String[] tables = new String[filteredTables.size()];
        for (int i = 0; i < filteredTables.size(); i++) {
            Table table = filteredTables.get(i);
            tables[i] = "Bàn " + table.getNumber() + " (" + table.getStatus() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn bàn")
                .setItems(tables, (dialog, which) -> {
                    currentTable = filteredTables.get(which).getNumber();  // Lấy từ filtered
                    Toast.makeText(this, "Đã chọn " + tables[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
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
        if (currentTable == 0 || orderItems.isEmpty()) {
            Toast.makeText(this, "Cần chọn bàn và món", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isNewOrder && currentOrderId > 0) {
            // Update order hiện tại (thêm items mới)
            for (OrderItem item : orderItems) {  // orderItems ở đây là items mới thêm (cũ đã load sẵn)
                orderItemDAO.addOrderItem(item.copy(currentOrderId));
            }
            // Update total (tính lại từ tất cả items)
            List<OrderItem> allItems = orderItemDAO.getOrderItemsByOrderId(currentOrderId);
            int newTotal = 0;
            for (OrderItem item : allItems) {
                Dish dish = dishDAO.getDishById(item.getDishId());
                if (dish != null) newTotal += dish.getPrice() * item.getQuantity();
            }
            orderDAO.updateOrderStatus(currentOrderId, "đang phục vụ");
            orderDAO.updateOrderTotal(currentOrderId, newTotal);  // Cập nhật total trong DB
            dbHelper.updateTableStatus(currentTable, "đang phục vụ");
            Toast.makeText(this, "Cập nhật order thành công!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            // Order mới
            Order order = new Order(0, currentTable, totalAmount);
            order.setStatus("đang phục vụ");
            long orderIdLong = orderDAO.addOrder(order);
            if (orderIdLong > 0) {
                dbHelper.updateTableStatus(currentTable, "đang phục vụ");
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {  // Từ DishDetail (thêm món)
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
        } else if (requestCode == 1 && resultCode == RESULT_OK) {  // Từ OrderSummary (lưu)
            // SỬA: Không finish() nữa, chỉ Toast thành công và refresh UI nếu cần
            // Ví dụ: Lưu đã gọi ở nơi khác (hoặc gọi saveOrder() ở đây nếu chưa)
            Toast.makeText(this, "Đã lưu order thành công! Tiếp tục chọn món?", Toast.LENGTH_SHORT).show();
            // Optional: Refresh tabs/dishes nếu cần
            if (!categories.isEmpty()) {
                loadDishes(categories.get(0).getId());  // Reload danh sách món
            }
            // Không gọi finish() → Ở lại Menu để thêm món tiếp
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}