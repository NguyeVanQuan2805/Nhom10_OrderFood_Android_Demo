package com.example.neworderfood.activitis;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.neworderfood.R;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neworderfood.adapters.TableStatusAdapter;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Table;

import java.util.List;

public class TableStatusActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TableStatusAdapter adapter;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_table_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.rv_table_status);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Table> tables = dbHelper.getAllTables();
        adapter = new TableStatusAdapter(tables);
        recyclerView.setAdapter(adapter);
    }
}