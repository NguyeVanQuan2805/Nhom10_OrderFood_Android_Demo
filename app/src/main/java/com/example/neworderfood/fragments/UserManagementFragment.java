package com.example.neworderfood.fragments;

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
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.UserDao;
import com.example.neworderfood.models.User;
import com.example.neworderfood.room.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> users = new ArrayList<>();
    private AppDatabase db;
    private UserDao userDAO;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        db = AppDatabase.getInstance(getContext());
        userDAO = db.userDao();

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
            // USE SYNC: Load all users sync
            List<UserEntity> entities = userDAO.getAllUsersSync();
            if (entities != null) {
                users.addAll(User.fromEntities(entities));
            }
            if (adapter != null) {
                adapter.updateUsers(users);  // Use update method in adapter
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
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setSelection(user.getRole().equals("admin") ? 0 : 1);

        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newUsername = etUsername.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();
            String newRole = spinnerRole.getSelectedItem().toString();

            if (newUsername.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Giữ password cũ nếu empty
            String finalPassword = newPassword.isEmpty() ? user.getPassword() : newPassword;

            user.setUsername(newUsername);
            user.setPassword(finalPassword);  // Hash nếu production
            user.setRole(newRole);

            UserEntity entity = user.toEntity();
            int result = userDAO.updateUser(entity);  // Delegate (thêm nếu chưa có)
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
        // UPDATED: Check if admin role - cannot delete
        if ("admin".equals(user.getRole())) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Không thể xóa")
                    .setMessage("Không thể xóa tài khoản admin!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa user '" + user.getUsername() + "'? (Không thể khôi phục)")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = userDAO.deleteUser(user.getId());  // Delegate (thêm nếu chưa có)
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
