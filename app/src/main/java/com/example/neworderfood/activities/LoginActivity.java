package com.example.neworderfood.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.neworderfood.MainActivity;
import com.example.neworderfood.R;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.UserDao;
import com.example.neworderfood.models.User;
import com.example.neworderfood.room.entities.UserEntity;

public class LoginActivity extends AppCompatActivity {
    private AppDatabase db;
    private UserDao userDAO;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        userDAO = db.userDao();
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Check if already logged in
        String role = prefs.getString("role", null);
        if (role != null) {
            if ("admin".equals(role)) {
                startActivity(new Intent(this, AdminManagementActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
            return;
        }

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            UserEntity entity = userDAO.getUserByUsernameAndPassword(username, password);
            if (entity != null) {
                User user = User.fromEntity(entity);
                String userRole = user.getRole();
                // Đổi tên biến để tránh conflict
                prefs.edit().putString("role", userRole).apply();

                if ("admin".equals(userRole)) {
                    startActivity(new Intent(this, AdminManagementActivity.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}