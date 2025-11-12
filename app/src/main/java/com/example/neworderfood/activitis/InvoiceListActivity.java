package com.example.neworderfood.activitis;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.neworderfood.R;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neworderfood.adapters.InvoiceAdapter;  // Tạo sau
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Invoice;

import java.util.List;

public class InvoiceListActivity extends AppCompatActivity {
    private RecyclerView rvInvoices;
    private DatabaseHelper dbHelper;
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
        dbHelper = new DatabaseHelper(this);
        setupToolbar();

        rvInvoices = findViewById(R.id.rv_invoices);
        rvInvoices.setLayoutManager(new LinearLayoutManager(this));

        loadInvoices();
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách Hóa đơn");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadInvoices() {
        List<Invoice> invoices = dbHelper.getAllInvoices();
        if (invoices.isEmpty()) {
            Toast.makeText(this, "Chưa có hóa đơn", Toast.LENGTH_SHORT).show();
            findViewById(R.id.tv_empty).setVisibility(View.VISIBLE);  // Thêm TextView empty nếu cần
            return;
        }
        rvInvoices.setAdapter(new InvoiceAdapter(invoices, invoice -> {
            Intent intent = new Intent(this, com.example.neworderfood.activitis.InvoiceDetailActivity.class);  // THÊM: Mở detail
            intent.putExtra("invoice", invoice);
            startActivity(intent);
        }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}