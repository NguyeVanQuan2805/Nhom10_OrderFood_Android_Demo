package com.example.neworderfood.fragment; // SỬA package nếu cần

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.adapters.AdminTableAdapter;
import com.example.neworderfood.activitis.AdminManagementActivity; // SỬA: activitis → activities
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.Table;

import java.util.ArrayList;
import java.util.List;

public class TableManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminTableAdapter adapter;
    private List<Table> tables = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_table_management, container, false);
        dbHelper = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.rv_tables);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadData();
        adapter = new AdminTableAdapter(tables, table -> {
            // SỬA: Gọi method trong fragment thay vì qua Activity (vì method private ở Activity)
            showEditTableDialog(table);
        });
        recyclerView.setAdapter(adapter); // SỬA: Di chuyển sau init adapter
        return view;
    }

    private void loadData() {
        tables.clear();
        tables.addAll(dbHelper.getAllTables());
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void refresh() {
        loadData();
    }

    // SỬA: Implement đầy đủ method trong fragment (không cần gọi từ Activity)
    public void showEditTableDialog(Table table) {
        if (getActivity() instanceof AdminManagementActivity) {
            ((AdminManagementActivity) getActivity()).showEditTableDialog(table);
        }
    }

    // THÊM: Method confirmDeleteTable trong fragment
    private void confirmDeleteTable(Table table) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa bàn " + table.getNumber() + " sẽ ảnh hưởng đến orders liên quan. Tiếp tục?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = dbHelper.deleteTable(table.getId());
                    if (result > 0) {
                        loadData();
                        refresh();
                        Toast.makeText(getContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi xóa (có thể có orders liên quan)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}