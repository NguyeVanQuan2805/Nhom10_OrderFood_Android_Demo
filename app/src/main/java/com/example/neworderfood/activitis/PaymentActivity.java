package com.example.neworderfood.activitis;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.neworderfood.R;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.OrderDAO;
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.models.OrderItem;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";

    private DatabaseHelper dbHelper;
    private OrderDAO orderDAO;
    private int totalAmount = 0;
    private int paidAmount = 0;
    private TextView tvTraLaiAmount;
    private TextView tvTotalAmount;
    private Button selectedButton = null;
    private List<OrderItem> orderItems;  // THÊM: Field cho items
    private int tableNumber;  // THÊM: Field cho tableNumber
    private int orderId;  // THÊM: Field cho orderId

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

        dbHelper = new DatabaseHelper(this);
        orderDAO = dbHelper.getOrderDAO();

        setupToolbar();

        orderId = getIntent().getIntExtra("order_id", 0);  // SỬA: Lấy orderId
        if (orderId <= 0) {  // SỬA: Kiểm tra orderId hợp lệ
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
            Snackbar.make(findViewById(android.R.id.content), "Lỗi: Không xác định bàn", Snackbar.LENGTH_SHORT).show();  // THAY Toast bằng Snackbar
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
                Snackbar.make(findViewById(android.R.id.content), "Số tiền phải >= tổng tiền!", Snackbar.LENGTH_SHORT).show();  // THAY Toast
                return;
            }
            // THÊM: Tạo invoice từ order hiện tại
            int change = paidAmount - totalAmount;
            Invoice invoice = new Invoice(0, new Date(), tableNumber, totalAmount, orderItems, change);

            // Lưu hóa đơn
            long invoiceId = dbHelper.addInvoice(invoice);
            if (invoiceId > 0) {
                // Giờ delete order cũ (như cũ)
                orderDAO.updateOrderStatus(orderId, "Paid");
                dbHelper.deleteOrderItemsByOrderId(orderId);
                dbHelper.deleteOrderById(orderId);

                // Update bàn về Available
                dbHelper.updateTableStatus(tableNumber, "Còn trống");  // SỬA: "Còn trống" thay vì "Available" cho nhất quán

                Snackbar.make(findViewById(android.R.id.content), "Hoàn thành thanh toán! Hóa đơn #" + invoiceId, Snackbar.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Lỗi lưu hóa đơn", Snackbar.LENGTH_SHORT).show();
            }
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