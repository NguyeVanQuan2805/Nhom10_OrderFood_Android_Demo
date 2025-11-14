package com.example.neworderfood.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neworderfood.R;
import com.example.neworderfood.adapters.DishAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.CategoryDao;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.room.daos.OrderDao;
import com.example.neworderfood.room.daos.OrderItemDao;
import com.example.neworderfood.room.daos.TableDao;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.models.Table;
import com.example.neworderfood.room.entities.CategoryEntity;
import com.example.neworderfood.room.entities.DishEntity;
import com.example.neworderfood.room.entities.OrderEntity;
import com.example.neworderfood.room.entities.OrderItemEntity;
import com.example.neworderfood.room.entities.TableEntity;
import com.google.android.material.tabs.TabLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuActivity extends AppCompatActivity {
    public interface OnSaveCompleteListener {
        void onComplete(int orderId);
    }

    private static final String TAG = "MenuActivity";
    private static final int ADD_DISH_REQUEST = 3;
    private RecyclerView recyclerView;
    private DishAdapter adapter;
    private AppDatabase db;
    private DishDao dishDAO;
    private OrderDao orderDAO;
    private OrderItemDao orderItemDAO;
    private CategoryDao categoryDAO;
    private TableDao tableDAO;
    private int currentOrderId = 0;
    private int currentTable = 0;
    private List<OrderItem> orderItems = new ArrayList<>();
    private int totalAmount = 0;
    private List<Category> categories = new ArrayList<>();
    private EditText etSearch;
    private boolean isNewOrder = true; // Default: order mới
    private ActivityResultLauncher<Intent> dishDetailLauncher;
    private ActivityResultLauncher<Intent> orderSummaryLauncher;

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

        // Initialize database and DAOs
        db = AppDatabase.getInstance(this);
        dishDAO = db.dishDao();
        orderDAO = db.orderDao();
        orderItemDAO = db.orderItemDao();
        categoryDAO = db.categoryDao();
        tableDAO = db.tableDao();

        // Initialize UI components
        initializeUI();

        // Setup activity result launchers
        setupActivityLaunchers();

        // Process intent data
        processIntentData(getIntent());

        // Load data
        loadCategoriesAsync();

        // Setup back pressed handler
        setupBackPressedHandler();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.rv_dishes);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView not found! Check layout XML");
            Toast.makeText(this, "Lỗi khởi tạo giao diện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupToolbar();
        setupSearch();
        setupButtons();
    }

    private void setupActivityLaunchers() {
        dishDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleDishDetailResult(result.getData());
                    }
                });
        orderSummaryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(this, "Đã lưu order thành công! Tiếp tục chọn món?", Toast.LENGTH_SHORT).show();
                        if (!categories.isEmpty()) {
                            loadDishes(categories.get(0).getId());
                        }
                    }
                });
    }

    private void handleDishDetailResult(Intent data) {
        final OrderItem item = (OrderItem) data.getSerializableExtra("item");
        if (item != null) {
            orderItems.add(item);
            final int currentTotal = totalAmount;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int addedPrice = 0;
                final DishEntity dishEntity = dishDAO.getDishById(item.getDishId());
                if (dishEntity != null) {
                    final Dish dish = Dish.fromEntity(dishEntity);
                    if (dish != null) {
                        final int dishPrice = dish.getPrice();
                        final int basePrice = dishPrice * item.getQuantity();
                        addedPrice = item.getDiscount() > 0 ? (int) (basePrice * (100 - item.getDiscount()) / 100.0) : basePrice;
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MenuActivity.this, "Không tìm thấy món ăn", Toast.LENGTH_SHORT).show();
                        orderItems.remove(item);
                        updateTotalDisplay(totalAmount);
                    });
                    executor.shutdown();
                    return;
                }
                final int finalAddedPrice = addedPrice;
                final int finalNewTotal = currentTotal + finalAddedPrice;
                runOnUiThread(() -> {
                    totalAmount = finalNewTotal;
                    updateTotalDisplay(finalNewTotal);
                });
            });
            executor.shutdown();
        }
    }

    private void updateTotalDisplay(int total) {
        TextView tvTotal = findViewById(R.id.tv_total);
        if (tvTotal != null) {
            tvTotal.setText(String.format("%,dđ", total));
        }
    }

    private void setupButtons() {
        ImageButton btnTable = findViewById(R.id.btn_table);
        if (btnTable != null) {
            btnTable.setOnClickListener(v -> showTableDialog(isNewOrder));
        }
        ImageButton btnDiscount = findViewById(R.id.btn_discount);
        if (btnDiscount != null) {
            btnDiscount.setOnClickListener(v -> showDiscountDialog());
        }
        ImageButton btnBill = findViewById(R.id.btn_bill);
        if (btnBill != null) {
            btnBill.setOnClickListener(v -> {
                if (orderItems.isEmpty()) {
                    Toast.makeText(this, "Chưa chọn món", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, OrderSummaryActivity.class);
                intent.putExtra("table", currentTable);
                intent.putExtra("total", totalAmount);
                intent.putExtra("items", (Serializable) orderItems);
                orderSummaryLauncher.launch(intent);
            });
        }
        Button btnLuu = findViewById(R.id.btn_luu);
        if (btnLuu != null) {
            btnLuu.setOnClickListener(v -> {
                if (currentTable == 0 || orderItems.isEmpty()) {
                    Toast.makeText(this, "Cần chọn bàn và món", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveOrder(null);
            });
        }
        Button btnThuTien = findViewById(R.id.btn_thu_tien);
        if (btnThuTien != null) {
            btnThuTien.setOnClickListener(v -> {
                if (currentTable == 0 || orderItems.isEmpty()) {
                    Toast.makeText(this, "Cần chọn bàn và món", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveOrder(orderId -> {
                    if (orderId > 0) {
                        Intent intent = new Intent(this, PaymentActivity.class);
                        intent.putExtra("order_id", orderId);
                        intent.putExtra("total_amount", totalAmount);
                        intent.putExtra("table_number", currentTable);
                        intent.putExtra("items", (Serializable) orderItems);
                        startActivity(intent);
                    }
                });
            });
        }
    }

    private void setupBackPressedHandler() {
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
                                    saveOrder(null);
                                }
                            })
                            .setNegativeButton("Thoát không lưu", (dialog, which) -> finish())
                            .setNeutralButton("Hủy", null)
                            .show();
                }
            }
        });
    }

    private void processIntentData(Intent intent) {
        isNewOrder = intent.getBooleanExtra("isNewOrder", true);
        final int passedOrderId = intent.getIntExtra("orderId", 0);
        final int passedTable = intent.getIntExtra("tableNumber", 0);
        if (!isNewOrder && passedOrderId > 0) {
            currentOrderId = passedOrderId;
            currentTable = passedTable;
            loadOrderItemsAsync(currentOrderId);
            findViewById(R.id.btn_table).setVisibility(View.GONE);
            Toast.makeText(this, "Đang gọi thêm món cho Bàn " + currentTable, Toast.LENGTH_SHORT).show();
        } else {
            currentTable = passedTable > 0 ? passedTable : 0;
            if (currentTable > 0) {
                loadExistingOrderForTableIfAny(currentTable);
            }
        }
    }

    private void loadExistingOrderForTableIfAny(int tableNumber) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                final List<OrderEntity> existingOrders = orderDAO.getUnpaidOrdersByTable(tableNumber);
                int tempOrderId = 0;
                if (existingOrders != null && !existingOrders.isEmpty()) {
                    tempOrderId = existingOrders.get(0).id;
                }
                final int finalTempOrderId = tempOrderId;
                runOnUiThread(() -> {
                    if (finalTempOrderId > 0) {
                        currentOrderId = finalTempOrderId;
                        loadOrderItemsAsync(finalTempOrderId);
                        Toast.makeText(MenuActivity.this, "Đã load order cũ cho Bàn " + tableNumber, Toast.LENGTH_SHORT).show();
                        findViewById(R.id.btn_table).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.btn_table).setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading existing order: " + e.getMessage(), e);
                runOnUiThread(() -> findViewById(R.id.btn_table).setVisibility(View.VISIBLE));
            }
        });
        executor.shutdown();
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

    private void loadCategoriesAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                final List<CategoryEntity> entityList = categoryDAO.getAllCategoriesSync();
                final List<Category> newCategories = entityList != null ? Category.fromEntities(entityList) : new ArrayList<>();
                runOnUiThread(() -> {
                    categories.clear();
                    categories.addAll(newCategories);
                    setupTabs();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading categories: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MenuActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_categories);
        if (tabLayout == null) {
            Log.e(TAG, "TabLayout not found!");
            return;
        }
        tabLayout.removeAllTabs();
        for (Category cat : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(cat.getName()));
        }
        if (!categories.isEmpty()) {
            loadDishes(categories.get(0).getId());
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                if (position >= 0 && position < categories.size()) {
                    final Category selectedCat = categories.get(position);
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
        if (recyclerView == null) return;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                final List<DishEntity> entityList = dishDAO.getDishesByCategorySync(categoryId);
                final List<Dish> dishesList = entityList != null ? Dish.fromEntities(entityList) : new ArrayList<>();
                runOnUiThread(() -> {
                    adapter = new DishAdapter(dishesList, dish -> {
                        final Intent intent = new Intent(MenuActivity.this, DishDetailActivity.class);
                        intent.putExtra("dish", dish);
                        intent.putExtra("categoryId", categoryId);
                        dishDetailLauncher.launch(intent);
                    });
                    recyclerView.setAdapter(adapter);
                    updateTotalDisplay(totalAmount);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading dishes: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MenuActivity.this, "Lỗi tải món ăn", Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
    }

    private void loadOrderItemsAsync(int orderId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<OrderItem> newOrderItems = new ArrayList<>();
            int newTotal = 0;
            try {
                final List<OrderItemEntity> entityItems = orderItemDAO.getOrderItemsByOrderId(orderId);
                if (entityItems != null) {
                    newOrderItems = OrderItem.fromEntities(entityItems);
                    for (OrderItem item : newOrderItems) {
                        final DishEntity dishEntity = dishDAO.getDishById(item.getDishId());
                        if (dishEntity != null) {
                            final Dish dish = Dish.fromEntity(dishEntity);
                            if (dish != null) {
                                final int basePrice = dish.getPrice() * item.getQuantity();
                                final int itemPrice = item.getDiscount() > 0 ? (int) (basePrice * (100 - item.getDiscount()) / 100.0) : basePrice;
                                newTotal += itemPrice;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading order items: " + e.getMessage(), e);
            }
            final List<OrderItem> finalOrderItems = newOrderItems;
            final int finalTotal = newTotal;
            runOnUiThread(() -> {
                orderItems = finalOrderItems;
                totalAmount = finalTotal;
                updateTotalDisplay(finalTotal);
            });
        });
        executor.shutdown();
    }

    private void showTableDialog(boolean onlyEmpty) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                final List<TableEntity> entityList = tableDAO.getAllTables();
                final List<Table> tablesList = Table.fromEntities(entityList);
                runOnUiThread(() -> {
                    if (tablesList.isEmpty()) {
                        Toast.makeText(MenuActivity.this, "Chưa có bàn nào. Vui lòng thêm bàn ở phần admin.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final List<Table> filteredTables = new ArrayList<>();
                    for (final Table table : tablesList) {
                        if (!onlyEmpty || "Còn trống".equals(table.getStatus())) {
                            filteredTables.add(table);
                        }
                    }
                    if (filteredTables.isEmpty()) {
                        Toast.makeText(MenuActivity.this, "Không còn bàn trống để order mới!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String[] tables = new String[filteredTables.size()];
                    for (int i = 0; i < filteredTables.size(); i++) {
                        final Table table2 = filteredTables.get(i);
                        tables[i] = "Bàn " + table2.getNumber() + " (" + table2.getStatus() + ")";
                    }
                    new AlertDialog.Builder(MenuActivity.this)
                            .setTitle("Chọn bàn")
                            .setItems(tables, (dialog, which) -> {
                                currentTable = filteredTables.get(which).getNumber();
                                loadExistingOrderForTableIfAny(currentTable);
                                Toast.makeText(MenuActivity.this, "Đã chọn " + tables[which], Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading tables: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MenuActivity.this, "Lỗi tải danh sách bàn", Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
    }

    private void showDiscountDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nhập % giảm (0-100)");
        new AlertDialog.Builder(this)
                .setTitle("Giảm giá (%)")
                .setView(input)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    final String discountStr = input.getText().toString().trim();
                    if (discountStr.isEmpty()) {
                        Toast.makeText(MenuActivity.this, "Vui lòng nhập % giảm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        final int discount = Integer.parseInt(discountStr);
                        if (discount < 0 || discount > 100) {
                            Toast.makeText(MenuActivity.this, "% giảm phải từ 0-100", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (totalAmount > 0) {
                            final int oldTotal = totalAmount;
                            totalAmount = (int) (oldTotal * (100 - discount) / 100.0);
                            updateTotalDisplay(totalAmount);
                            Toast.makeText(MenuActivity.this, "Áp dụng giảm " + discount + "%", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(MenuActivity.this, "Số không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveOrder(OnSaveCompleteListener listener) {
        if (currentTable == 0 || orderItems.isEmpty()) {
            Toast.makeText(this, "Cần chọn bàn và món", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onComplete(0);
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                int savedOrderId = currentOrderId;
                if (currentOrderId > 0) {
                    // Update existing order
                    Log.d(TAG, "Updating existing order ID: " + currentOrderId);
                    // Delete existing order items to avoid duplicate IDs
                    orderItemDAO.deleteOrderItemsByOrderId(currentOrderId);
                    for (OrderItem item : orderItems) {
                        OrderItemEntity entity = item.toEntity();
                        entity.id = 0; // Reset ID for new insert
                        entity.orderId = currentOrderId;
                        long addResult = orderItemDAO.addOrderItem(entity);
                        if (addResult <= 0) {
                            Log.e(TAG, "Failed to add item for order " + currentOrderId);
                        }
                    }
                    // Recalc total
                    List<OrderItemEntity> allEntityItems = orderItemDAO.getOrderItemsByOrderId(currentOrderId);
                    List<OrderItem> allItems = OrderItem.fromEntities(allEntityItems);
                    int newTotal = 0;
                    for (OrderItem item : allItems) {
                        DishEntity dishEntity = dishDAO.getDishById(item.getDishId());
                        if (dishEntity != null) {
                            Dish dish = Dish.fromEntity(dishEntity);
                            if (dish != null) {
                                int basePrice = dish.getPrice() * item.getQuantity();
                                int itemPrice = item.getDiscount() > 0 ? (int) (basePrice * (100 - item.getDiscount()) / 100.0) : basePrice;
                                newTotal += itemPrice;
                            }
                        }
                    }
                    int updateTotalResult = orderDAO.updateOrderTotal(currentOrderId, newTotal);
                    if (updateTotalResult > 0) {
                        orderDAO.updateOrderStatus(currentOrderId, "đang phục vụ");
                        tableDAO.updateTableStatusByNumber(currentTable, "đang phục vụ");
                        final int finalNewTotal = newTotal;
                        final int finalSavedOrderId = savedOrderId;
                        final OnSaveCompleteListener finalListener = listener;
                        runOnUiThread(() -> {
                            totalAmount = finalNewTotal;
                            Toast.makeText(MenuActivity.this, "Cập nhật order thành công! Total: " + finalNewTotal, Toast.LENGTH_SHORT).show();
                            if (finalListener != null) {
                                finalListener.onComplete(finalSavedOrderId);
                            } else {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                        return;
                    } else {
                        Log.e(TAG, "Failed to update total for order " + currentOrderId);
                        final OnSaveCompleteListener finalListener = listener;
                        runOnUiThread(() -> {
                            Toast.makeText(MenuActivity.this, "Lỗi cập nhật total order", Toast.LENGTH_SHORT).show();
                            if (finalListener != null) finalListener.onComplete(0);
                        });
                        return;
                    }
                } else {
                    // Create new order
                    Log.d(TAG, "Creating new order for table: " + currentTable);
                    OrderEntity orderEntity = new OrderEntity(0, currentTable, totalAmount, "đang phục vụ", null);
                    long orderIdLong = orderDAO.addOrder(orderEntity);
                    if (orderIdLong > 0) {
                        tableDAO.updateTableStatusByNumber(currentTable, "đang phục vụ");
                        savedOrderId = (int) orderIdLong;
                        currentOrderId = savedOrderId;
                        for (OrderItem item : orderItems) {
                            OrderItemEntity entity = item.toEntity();
                            entity.id = 0; // Ensure new ID
                            entity.orderId = savedOrderId;
                            long addItemResult = orderItemDAO.addOrderItem(entity);
                            if (addItemResult <= 0) {
                                Log.e(TAG, "Failed to add item " + item.getId() + " to new order " + savedOrderId);
                            }
                        }
                        final int finalSavedOrderId = savedOrderId;
                        final OnSaveCompleteListener finalListener = listener;
                        runOnUiThread(() -> {
                            Toast.makeText(MenuActivity.this, "Lưu thành công! Order ID: " + finalSavedOrderId, Toast.LENGTH_SHORT).show();
                            if (finalListener != null) {
                                finalListener.onComplete(finalSavedOrderId);
                            } else {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                        return;
                    } else {
                        Log.e(TAG, "Failed to create new order for table " + currentTable + " - check DB constraints");
                        final OnSaveCompleteListener finalListener = listener;
                        runOnUiThread(() -> {
                            Toast.makeText(MenuActivity.this, "Lỗi tạo order mới", Toast.LENGTH_SHORT).show();
                            if (finalListener != null) finalListener.onComplete(0);
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving order: " + e.getMessage(), e);
                final OnSaveCompleteListener finalListener = listener;
                runOnUiThread(() -> {
                    Toast.makeText(MenuActivity.this, "Lỗi lưu order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (finalListener != null) finalListener.onComplete(0);
                });
            }
        });
        executor.shutdown();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}