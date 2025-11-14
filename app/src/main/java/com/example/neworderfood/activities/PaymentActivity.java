package com.example.neworderfood.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.neworderfood.R;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.InvoiceDao;
import com.example.neworderfood.room.daos.InvoiceItemDao;
import com.example.neworderfood.room.daos.OrderDao;
import com.example.neworderfood.room.daos.OrderItemDao;
import com.example.neworderfood.room.daos.TableDao;
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.room.entities.InvoiceEntity;
import com.example.neworderfood.room.entities.InvoiceItemEntity;
import com.example.neworderfood.room.entities.OrderItemEntity;
import com.google.android.material.snackbar.Snackbar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    private AppDatabase db;
    private OrderDao orderDAO;
    private OrderItemDao orderItemDAO;
    private InvoiceItemDao invoiceItemDao;
    private InvoiceDao invoiceDAO;
    private TableDao tableDAO;
    private int totalAmount = 0;
    private int paidAmount = 0;
    private TextView tvTraLaiAmount;
    private TextView tvTotalAmount;
    private Button selectedButton = null;
    private List<OrderItem> orderItems; // THÊM: Field cho items
    private int tableNumber; // THÊM: Field cho tableNumber
    private int orderId; // THÊM: Field cho orderId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        orderDAO = db.orderDao();
        orderItemDAO = db.orderItemDao();
        invoiceItemDao = db.invoiceItemDao();
        invoiceDAO = db.invoiceDao();
        tableDAO = db.tableDao();
        setupToolbar();

        orderId = getIntent().getIntExtra("order_id", 0); // SỬA: Lấy orderId
        if (orderId <= 0) { // SỬA: Kiểm tra orderId hợp lệ
            Log.e(TAG, "Invalid orderId: " + orderId);
            Snackbar.make(findViewById(android.R.id.content), "Lỗi: Order không hợp lệ", Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        totalAmount = getIntent().getIntExtra("total_amount", 0);

        // THÊM: Lấy tableNumber và orderItems từ Intent
        tableNumber = getIntent().getIntExtra("table_number", 0);
        if (tableNumber == 0) {
            Log.w(TAG, "No table number provided!");
            Snackbar.make(findViewById(android.R.id.content), "Lỗi: Không xác định bàn", Snackbar.LENGTH_SHORT).show();
            // THAY Toast bằng Snackbar
            finish();
            return;
        }

        orderItems = (List<OrderItem>) getIntent().getSerializableExtra("items");
        if (orderItems == null || orderItems.isEmpty()) {
            Log.w(TAG, "No order items provided!");
            Snackbar.make(findViewById(android.R.id.content), "Lỗi: Không có món hàng", Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvTotalAmount.setText(String.format("%,dđ", totalAmount));

        tvTraLaiAmount = findViewById(R.id.tv_tra_lai_amount);
        updateTraLai();

        setupPaymentButton(R.id.btn_35k, 35000);
        setupPaymentButton(R.id.btn_40k, 40000);
        setupPaymentButton(R.id.btn_50k, 50000);
        setupPaymentButton(R.id.btn_100k, 100000);
        setupPaymentButton(R.id.btn_200k, 200000);
        setupPaymentButton(R.id.btn_500k, 500000);

        Button btnHoanThanh = findViewById(R.id.btn_hoan_thanh);
        btnHoanThanh.setOnClickListener(v -> {
            if (paidAmount < totalAmount) {
                Snackbar.make(findViewById(android.R.id.content), "Số tiền phải >= tổng tiền!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent multiple clicks
            btnHoanThanh.setEnabled(false);
            btnHoanThanh.setText("Đang xử lý...");

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                db.beginTransaction();
                try {
                    // Update order status to "Paid" first
                    int statusUpdate = orderDAO.updateOrderStatus(orderId, "Paid");
                    Log.d(TAG, "Status update for order " + orderId + ": " + statusUpdate);

                    // Tạo invoice với id=0
                    int change = paidAmount - totalAmount;
                    Invoice tempInvoice = new Invoice(0, new Date(), tableNumber, totalAmount, orderItems, change);

                    // Insert parent và lấy id mới
                    InvoiceEntity invoiceEntity = tempInvoice.toEntity();
                    long invoiceIdLong = invoiceDAO.addInvoice(invoiceEntity);
                    if (invoiceIdLong <= 0) {
                        throw new RuntimeException("Failed to insert invoice");
                    }
                    int invoiceId = (int) invoiceIdLong;
                    Log.d(TAG, "Inserted invoice ID: " + invoiceId);

                    // Update invoice.id và recreate items
                    tempInvoice.setId(invoiceId);
                    List<InvoiceItemEntity> itemEntities = tempInvoice.getItemsAsEntities();

                    // Insert items
                    int insertedItems = 0;
                    for (InvoiceItemEntity itemEntity : itemEntities) {
                        long itemId = invoiceItemDao.addInvoiceItem(itemEntity);
                        if (itemId > 0) insertedItems++;
                        Log.d(TAG, "Inserted item ID: " + itemId + " for invoice " + invoiceId);
                    }
                    if (insertedItems != itemEntities.size()) {
                        throw new RuntimeException("Failed to insert all items");
                    }

                    // Xóa order items
                    List<OrderItemEntity> entityItems = orderItemDAO.getOrderItemsByOrderId(orderId);
                    for (OrderItemEntity entity : entityItems) {
                        int delResult = orderItemDAO.deleteOrderItem(entity.getId());
                        Log.d(TAG, "Deleted order item ID " + entity.getId() + ": " + delResult);
                    }

                    // Xóa order (sau update status, ID match)
                    int orderDel = orderDAO.deleteOrderById(orderId);
                    Log.d(TAG, "Deleted order ID " + orderId + ": " + orderDel);
                    if (orderDel <= 0) {
                        Log.w(TAG, "Delete order failed - check ID/status");
                    }

                    // Update table
                    tableDAO.updateTableStatusByNumber(tableNumber, "Còn trống");

                    db.setTransactionSuccessful();
                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), "Hoàn thành thanh toán! Hóa đơn #" + invoiceId, Snackbar.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    db.endTransaction();
                    Log.e(TAG, "Transaction failed", e);
                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), "Lỗi thanh toán: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        btnHoanThanh.setEnabled(true);
                        btnHoanThanh.setText("HOÀN THÀNH");
                    });
                } finally {
                    db.endTransaction();
                }
            });
            executor.shutdown();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (paidAmount < totalAmount) {
                    new AlertDialog.Builder(PaymentActivity.this)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn muốn thoát? Thay đổi sẽ không được lưu.")
                            .setPositiveButton("Thoát", (dialog, which) -> finish())
                            .setNegativeButton("Hủy", null)
                            .show();
                } else {
                    finish();
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
            getSupportActionBar().setTitle("Thu tiền");
        }
    }

    private void setupPaymentButton(int buttonId, int amount) {
        Button btn = findViewById(buttonId);
        btn.setOnClickListener(v -> {
            if (selectedButton != null && selectedButton != btn) {
                selectedButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
            }
            paidAmount = amount;
            updateTraLai();
            selectedButton = btn;
            int change = paidAmount - totalAmount;
            if (change > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Tiền thừa: " + String.format("%,dđ", change))
                        .setPositiveButton("OK", null)
                        .show();
            }
            btn.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_light));
        });
    }

    private void updateTraLai() {
        int change = paidAmount - totalAmount;
        tvTraLaiAmount.setText(String.format("%,dđ", Math.max(0, change)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}