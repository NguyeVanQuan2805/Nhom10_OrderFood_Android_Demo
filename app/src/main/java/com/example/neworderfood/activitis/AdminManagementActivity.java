package com.example.neworderfood.activitis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.neworderfood.activitis.LoginActivity;
import com.example.neworderfood.R;
import com.example.neworderfood.adapters.AdminPagerAdapter;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.CategoryDAO;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.fragment.AdminCategoryFragment;
import com.example.neworderfood.fragment.AdminDishFragment;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AdminManagementActivity extends AppCompatActivity {
    private static final String TAG = "AdminManagementActivity";
    private DatabaseHelper dbHelper;
    private CategoryDAO categoryDAO;
    private DishDAO dishDAO;
    private List<Category> categories = new ArrayList<>();
    private List<Dish> dishes = new ArrayList<>();
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_management);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        categoryDAO = dbHelper.getCategoryDAO();
        dishDAO = dbHelper.getDishDAO();

        setupToolbar();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fabAdd = findViewById(R.id.fab_add);

        loadData();
        setupViewPager();
        setupFab();

        fabAdd.setOnClickListener(v -> {
            int selectedTab = tabLayout.getSelectedTabPosition();
            if (selectedTab == 0) {
                showAddCategoryDialog();
            } else {
                showAddDishDialog();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý");
        }
    }

    private void loadData() {
        categories.clear();
        categories.addAll(categoryDAO.getAllCategories());
        dishes.clear();
        dishes.addAll(dishDAO.getAllDishes());
        Log.d(TAG, "Loaded " + categories.size() + " categories and " + dishes.size() + " dishes");
    }

    private void setupViewPager() {
        AdminPagerAdapter adapter = new AdminPagerAdapter(this, categories, dishes, categoryDAO, dishDAO);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Danh mục" : "Món ăn");
        }).attach();
    }

    private void setupFab() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    fabAdd.setImageResource(R.drawable.ic_add_category);  // Assume icon
                } else {
                    fabAdd.setImageResource(R.drawable.ic_add_dish);  // Assume icon
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm danh mục");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = view.findViewById(R.id.et_name);
        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                Category cat = new Category(0, name);
                if (categoryDAO.addCategory(cat) > 0) {
                    loadData();
                    refreshFragment(0);  // Refresh CategoryFragment
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi thêm", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Tên không được rỗng", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showAddDishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm món ăn");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_dish, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPrice = view.findViewById(R.id.et_price);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            if (!name.isEmpty() && !priceStr.isEmpty()) {
                try {
                    int price = Integer.parseInt(priceStr);
                    Category selectedCat = (Category) spinnerCategory.getSelectedItem();
                    if (selectedCat != null) {
                        // SỬA: Thêm imageResource = 0 (placeholder)
                        Dish dish = new Dish(0, name, price, selectedCat.getId(), 0);
                        if (dishDAO.addDish(dish) > 0) {
                            loadData();
                            refreshFragment(1);  // Refresh DishFragment
                            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi thêm", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Chọn danh mục", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Method để refresh fragment cụ thể (gọi từ dialog sau add/edit/delete)
    private void refreshFragment(int position) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + position);
        if (fragment instanceof AdminCategoryFragment && position == 0) {
            ((AdminCategoryFragment) fragment).refresh();
        } else if (fragment instanceof AdminDishFragment && position == 1) {
            ((AdminDishFragment) fragment).refresh();
        }
    }

    // Method cho edit category (gọi từ adapter long click)
    public void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa danh mục");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = view.findViewById(R.id.et_name);
        etName.setText(category.getName());
        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                category.setName(name);
                if (categoryDAO.updateCategory(category) > 0) {
                    loadData();
                    refreshFragment(0);
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.setNeutralButton("Xóa", (dialog, which) -> confirmDeleteCategory(category));
        builder.show();
    }

    // Method cho edit dish (tương tự, với spinner preselect)
    public void showEditDishDialog(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa món ăn");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_dish, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPrice = view.findViewById(R.id.et_price);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
        // Preselect category
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == dish.getCategoryId()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        etName.setText(dish.getName());
        etPrice.setText(String.valueOf(dish.getPrice()));
        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            if (!name.isEmpty() && !priceStr.isEmpty()) {
                try {
                    int price = Integer.parseInt(priceStr);
                    Category selectedCat = (Category) spinnerCategory.getSelectedItem();
                    if (selectedCat != null) {
                        dish.setName(name);
                        dish.setPrice(price);
                        dish.setCategoryId(selectedCat.getId());
                        // Giữ nguyên imageResource cũ hoặc set mới nếu có field
                        if (dishDAO.updateDish(dish) > 0) {
                            loadData();
                            refreshFragment(1);
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.setNeutralButton("Xóa", (dialog, which) -> confirmDeleteDish(dish));
        builder.show();
    }

    private void confirmDeleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa danh mục sẽ ảnh hưởng đến món ăn liên quan. Tiếp tục?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (categoryDAO.deleteCategory(category.getId()) > 0) {
                        loadData();
                        refreshFragment(0);
                        refreshFragment(1);  // Refresh dishes vì FK
                        Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi xóa (có thể có món liên quan)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteDish(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa món ăn này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (dishDAO.deleteDish(dish.getId()) > 0) {
                        loadData();
                        refreshFragment(1);
                        Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();  // Reload data khi resume (sau edit/delete)
    }
}