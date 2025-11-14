package com.example.neworderfood.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
import com.example.neworderfood.adapters.OrderItemAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.models.OrderItem;
import java.util.ArrayList;
import java.util.List;

public class OrderSummaryActivity extends AppCompatActivity {
    private AppDatabase db;
    private DishDao dishDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        dishDAO = db.dishDao();

        setupToolbar();

        int table = getIntent().getIntExtra("table", 0);
        int total = getIntent().getIntExtra("total", 0);
        List<OrderItem> items = (List<OrderItem>) getIntent().getSerializableExtra("items");
        if (items == null) {
            items = new ArrayList<>();
        }

        TextView tvTable = findViewById(R.id.tv_table);
        tvTable.setText("Bàn " + table + " (SL: " + items.size() + ")");

        TextView tvTotal = findViewById(R.id.tv_total);
        tvTotal.setText(String.format("%,dđ", total));

        RecyclerView rvItems = findViewById(R.id.rv_order_items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(new OrderItemAdapter(items, dishDAO));

        Button btnLuu = findViewById(R.id.btn_luu);
        btnLuu.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tóm tắt Order");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}