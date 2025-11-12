package com.example.neworderfood.activitis;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.adapters.OrderItemAdapter;  // Reuse existing adapter for items
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceDetailActivity extends AppCompatActivity {
    private static final String TAG = "InvoiceDetailActivity";

    private TextView tvInvoiceNumber, tvDate, tvTable, tvTotal, tvChange;
    private RecyclerView rvItems;
    private DatabaseHelper dbHelper;
    private DishDAO dishDAO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(this);
        dishDAO = dbHelper.getDishDAO();

        setupToolbar();

        // Lấy Invoice từ Intent
        Invoice invoice = (Invoice) getIntent().getSerializableExtra("invoice");
        if (invoice == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind data
        tvInvoiceNumber = findViewById(R.id.tv_invoice_number);
        tvDate = findViewById(R.id.tv_date);
        tvTable = findViewById(R.id.tv_table);
        tvTotal = findViewById(R.id.tv_total);
        tvChange = findViewById(R.id.tv_change);

        tvInvoiceNumber.setText("Số: " + String.format("%06d", invoice.getId()));  // Format 000006
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(invoice.getDate()));
        tvTable.setText("Bàn " + invoice.getTableNumber());
        tvTotal.setText(String.format("%,dđ", invoice.getTotalAmount()));
        tvChange.setText(String.format("%,dđ", invoice.getChangeAmount()));

        // RecyclerView cho items
        rvItems = findViewById(R.id.rv_invoice_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(new OrderItemAdapter(invoice.getItems(), dishDAO));  // Reuse adapter, hiển thị notes như "- Rau trần"
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết Hóa đơn");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}