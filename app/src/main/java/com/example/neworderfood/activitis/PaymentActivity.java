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

public class PaymentActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private OrderDAO orderDAO;
    private int totalAmount = 0;
    private int paidAmount = 0;
    private TextView tvTraLaiAmount;
    private TextView tvTotalAmount;
    private Button selectedButton = null;

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

        int orderId = getIntent().getIntExtra("order_id", 0);
        totalAmount = getIntent().getIntExtra("total_amount", 0);

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
                Toast.makeText(this, "Số tiền phải >= tổng tiền!", Toast.LENGTH_SHORT).show();
                return;
            }
            orderDAO.updateOrderStatus(orderId, "Paid");
            dbHelper.deleteOrderItemsByOrderId(orderId);  // Sử dụng delegate từ helper
            Log.d("PaymentActivity", "Deleted all OrderItems for order " + orderId);
            dbHelper.deleteOrderById(orderId);
            Log.d("PaymentActivity", "Deleted Order " + orderId);
            Toast.makeText(this, "Hoàn thành thanh toán!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
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