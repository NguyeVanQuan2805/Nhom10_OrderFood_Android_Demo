package com.example.neworderfood.activities;

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
import com.example.neworderfood.adapters.OrderItemAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.room.daos.InvoiceItemDao;  // FIX: Import DAO
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.room.entities.InvoiceItemEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvoiceDetailActivity extends AppCompatActivity {
    private static final String TAG = "InvoiceDetailActivity";

    private TextView tvInvoiceNumber, tvDate, tvTable, tvTotal, tvChange;
    private RecyclerView rvItems;
    private AppDatabase db;
    private DishDao dishDAO;
    private InvoiceItemDao invoiceItemDao;  // FIX: Declare

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

        // FIX: Init DB và DAOs (thêm invoiceItemDao)
        db = AppDatabase.getInstance(this);
        dishDAO = db.dishDao();
        invoiceItemDao = db.invoiceItemDao();  // FIX: Init để tránh NPE

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

        tvInvoiceNumber.setText("Số: " + String.format("%06d", invoice.getId())); // Format 000006
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(invoice.getDate()));
        tvTable.setText("Bàn " + invoice.getTableNumber());
        tvTotal.setText(String.format("%,dđ", invoice.getTotalAmount()));
        tvChange.setText(String.format("%,dđ", invoice.getChangeAmount()));

        // RecyclerView cho items
        rvItems = findViewById(R.id.rv_invoice_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        // FIX: Load items async với sync query + try-catch tránh crash
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                if (invoiceItemDao == null) {
                    Log.e(TAG, "invoiceItemDao is null! Init failed.");
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi DAO: Không tải được items", Toast.LENGTH_SHORT).show());
                    return;
                }
                List<InvoiceItemEntity> itemEntities = invoiceItemDao.getInvoiceItemsByInvoiceId(invoice.getId());
                Log.d(TAG, "Loaded " + itemEntities.size() + " items for invoice " + invoice.getId());
                List<OrderItem> loadedItems = OrderItem.fromInvoiceItems(itemEntities);
                Log.d(TAG, "Mapped to " + loadedItems.size() + " OrderItems");

                final List<OrderItem> finalItems = loadedItems != null ? loadedItems : new ArrayList<>();
                runOnUiThread(() -> {
                    if (finalItems.isEmpty()) {
                        Toast.makeText(this, "Không có món trong hóa đơn", Toast.LENGTH_SHORT).show();
                    }
                    rvItems.setAdapter(new OrderItemAdapter(finalItems, dishDAO));  // Set adapter on UI
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading invoice items", e);
                runOnUiThread(() -> Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
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