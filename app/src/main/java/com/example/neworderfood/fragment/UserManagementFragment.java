package com.example.neworderfood.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.R;
import com.example.neworderfood.adapters.AdminUserAdapter;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> users = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);
        dbHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.rv_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        // Init adapter với callback cho edit/delete
        adapter = new AdminUserAdapter(users, user -> {
            // Hiển thị dialog edit hoặc delete (có thể dùng AlertDialog để chọn)
            showUserActionDialog(user);
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadData() {
        try {
            users.clear();
            List<User> loadedUsers = dbHelper.getAllUsers();  // Delegate từ DatabaseHelper
            if (loadedUsers != null) {
                users.addAll(loadedUsers);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu users", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void refresh() {
        loadData();
    }

    // THÊM: Dialog để chọn edit hoặc delete khi long click
    private void showUserActionDialog(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Quản lý User: " + user.getUsername())
                .setItems(new String[]{"Sửa", "Xóa"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditUserDialog(user);
                    } else {
                        showDeleteUserDialog(user);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // THÊM: Method edit user (gọi từ Activity nếu cần, nhưng giữ trong fragment)
    private void showEditUserDialog(User user) {
        // Inflate dialog (tạo dialog_edit_user.xml tương tự dialog_add_table.xml với EditText username/password, Spinner role)
        // Ví dụ đơn giản với AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sửa User");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);  // Giả sử có layout này
        EditText etUsername = view.findViewById(R.id.et_username);
        EditText etPassword = view.findViewById(R.id.et_password);
        Spinner spinnerRole = view.findViewById(R.id.spinner_role);

        // Prefill
        etUsername.setText(user.getUsername());
        etPassword.setText("");  // Không prefill password (security)
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"admin", "employee"});
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setSelection(user.getRole().equals("admin") ? 0 : 1);

        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newUsername = etUsername.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();
            String newRole = spinnerRole.getSelectedItem().toString();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setUsername(newUsername);
            user.setPassword(newPassword);  // Hash nếu production
            user.setRole(newRole);

            int result = dbHelper.updateUser(user);  // Delegate (thêm nếu chưa có)
            if (result > 0) {
                loadData();
                refresh();
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // THÊM: Method delete user
    private void showDeleteUserDialog(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa user '" + user.getUsername() + "'? (Không thể khôi phục)")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = dbHelper.deleteUser(user.getId());  // Delegate (thêm nếu chưa có)
                    if (result > 0) {
                        loadData();
                        refresh();
                        Toast.makeText(getContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi xóa user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}