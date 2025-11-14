package com.example.neworderfood.activities;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.neworderfood.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neworderfood.adapters.TableStatusAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.TableDao;
import com.example.neworderfood.models.Table;
import com.example.neworderfood.room.entities.TableEntity;
import java.util.List;

public class TableStatusActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TableStatusAdapter adapter;
    private AppDatabase db;
    private TableDao tableDAO;

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

        db = AppDatabase.getInstance(this);
        tableDAO = db.tableDao();

        recyclerView = findViewById(R.id.rv_table_status);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<TableEntity> entities = tableDAO.getAllTables();
        List<Table> tables = Table.fromEntities(entities);

        adapter = new TableStatusAdapter(tables);
        recyclerView.setAdapter(adapter);
    }
}