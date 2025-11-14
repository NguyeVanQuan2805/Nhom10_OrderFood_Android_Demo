package com.example.neworderfood.activities;  // SỬA: Package đúng là "activities"

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.adapters.InvoiceAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.InvoiceDao;
import com.example.neworderfood.room.daos.InvoiceItemDao;  // THÊM: Import InvoiceItemDao với full qualified nếu cần
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.room.entities.InvoiceEntity;
import com.example.neworderfood.room.entities.InvoiceItemEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvoiceListActivity extends AppCompatActivity {
    private RecyclerView rvInvoices;
    private TextView tvEmpty;  // THÊM: Reference cho tv_empty
    private AppDatabase db;
    private InvoiceDao invoiceDAO;
    private InvoiceItemDao invoiceItemDAO;  // SỬA: Đảm bảo type match với DAO interface

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        invoiceDAO = db.invoiceDao();
        invoiceItemDAO = db.invoiceItemDao();  // SỬA: Gọi đúng abstract method từ AppDatabase

        setupToolbar();
        rvInvoices = findViewById(R.id.rv_invoices);
        tvEmpty = findViewById(R.id.tv_empty);  // THÊM: Init tv_empty
        if (rvInvoices != null) {
            rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        }
        loadInvoicesAsync();  // SỬA: Load async
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách Hóa đơn");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);  // THÊM: Cho back button
        }
    }

    // SỬA: Load async để tránh block UI
    private void loadInvoicesAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Lấy danh sách entity từ DB
            List<InvoiceEntity> invoiceEntities = invoiceDAO.getAllInvoices();

            // Lấy toàn bộ items
            List<InvoiceItemEntity> allInvoiceItems = invoiceItemDAO.getAllInvoiceItems();

            // Chuyển đổi sang List<Invoice>
            List<Invoice> invoices = Invoice.fromEntities(invoiceEntities, allInvoiceItems);

            runOnUiThread(() -> {
                if (invoices.isEmpty()) {
                    Toast.makeText(this, "Chưa có hóa đơn", Toast.LENGTH_SHORT).show();
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                    if (rvInvoices != null) {
                        rvInvoices.setVisibility(View.GONE);
                    }
                    return;
                }
                if (tvEmpty != null) {
                    tvEmpty.setVisibility(View.GONE);
                }
                if (rvInvoices != null) {
                    rvInvoices.setVisibility(View.VISIBLE);
                    rvInvoices.setAdapter(new InvoiceAdapter(invoices, invoice -> {
                        Intent intent = new Intent(this, InvoiceDetailActivity.class);
                        intent.putExtra("invoice", invoice);
                        startActivity(intent);
                    }));
                }
            });
        });
        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvoicesAsync();  // THÊM: Reload khi resume
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}