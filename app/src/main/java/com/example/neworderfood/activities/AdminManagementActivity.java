package com.example.neworderfood.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.neworderfood.R;
import com.example.neworderfood.adapters.AdminPagerAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.CategoryDao;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.room.daos.TableDao;
import com.example.neworderfood.room.daos.UserDao;
import com.example.neworderfood.room.entities.CategoryEntity;
import com.example.neworderfood.room.entities.DishEntity;
import com.example.neworderfood.room.entities.TableEntity;
import com.example.neworderfood.room.entities.UserEntity;
import com.example.neworderfood.fragments.AdminCategoryFragment;
import com.example.neworderfood.fragments.AdminDishFragment;
import com.example.neworderfood.fragments.TableManagementFragment;
import com.example.neworderfood.fragments.UserManagementFragment;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.Table;
import com.example.neworderfood.models.User;
import com.example.neworderfood.utils.ImageUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminManagementActivity extends AppCompatActivity {
    private static final String TAG = "AdminManagementActivity";
    // Constants for permissions and image picking
    private static final int REQUEST_CAMERA_PERMISSION = 1003;
    private static final int REQUEST_STORAGE_PERMISSION = 1004;

    private AppDatabase db;
    private CategoryDao categoryDAO;
    private DishDao dishDAO;
    private TableDao tableDAO;
    private UserDao userDAO;
    private List<Category> categories = new ArrayList<>();
    private List<Dish> dishes = new ArrayList<>();
    private List<Table> tables = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAdd;

    // Variables for image handling
    private String currentImageBase64;
    private Dish currentEditingDish;
    private ImageView currentImageView;

    // Flags for permission handling in image picker
    private boolean needCameraForPicker = false;
    private boolean needStorageForPicker = false;

    // FIXED: Moved Uri declaration to class level earlier to avoid use before declaration
    private Uri currentImageUri;

    // FIXED: Use Activity Result API instead of deprecated onActivityResult
    private final ActivityResultLauncher<Intent> imageCaptureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // No data needed because we use EXTRA_OUTPUT
                    handleCapturedImage();
                }
            });

    private final ActivityResultLauncher<Intent> imagePickLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleSelectedImage(imageUri);
                    }
                }
            });

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

        // Init Room DB and DAOs
        db = AppDatabase.getInstance(this);
        categoryDAO = db.categoryDao();
        dishDAO = db.dishDao();
        tableDAO = db.tableDao();
        userDAO = db.userDao();

        setupToolbar();
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fabAdd = findViewById(R.id.fab_add);
        loadDataAsync();  // FIXED: Load async to avoid UI block
        setupViewPager();
        setupFab();

        fabAdd.setOnClickListener(v -> {
            int selectedTab = tabLayout.getSelectedTabPosition();
            switch (selectedTab) {
                case 0:
                    showAddCategoryDialog();
                    break;
                case 1:
                    showAddDishDialog();
                    break;
                case 2:
                    showAddTableDialog();
                    break;
                case 3:
                    showAddUserDialog();
                    break;
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // FIXED: Load data async to avoid blocking UI thread
    private void loadDataAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Load categories - USE SYNC
            List<CategoryEntity> catEntities = categoryDAO.getAllCategoriesSync();
            if (catEntities != null) {
                categories.clear();
                categories.addAll(Category.fromEntities(catEntities));
            }

            // Load dishes - USE SYNC
            List<DishEntity> dishEntities = dishDAO.getAllDishesSync();
            if (dishEntities != null) {
                dishes.clear();
                dishes.addAll(Dish.fromEntities(dishEntities));
            }

            // Load tables - ALREADY SYNC
            List<TableEntity> tableEntities = tableDAO.getAllTables();
            if (tableEntities != null) {
                tables.clear();
                tables.addAll(Table.fromEntities(tableEntities));
            }

            // Load users - USE SYNC
            List<UserEntity> userEntities = userDAO.getAllUsersSync();
            if (userEntities != null) {
                users.clear();
                users.addAll(User.fromEntities(userEntities));
            }

            runOnUiThread(() -> {
                Log.d(TAG, "Loaded data: cats=" + categories.size() + ", dishes=" + dishes.size() +
                        ", tables=" + tables.size() + ", users=" + users.size());
                // FIXED: Refresh all fragments by finding by tag and calling refresh() to update display
                if (viewPager.getAdapter() != null) {
                    for (int i = 0; i < viewPager.getAdapter().getItemCount(); i++) {
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + i);
                        if (fragment != null) {
                            refreshSpecificFragment(fragment, i);
                        }
                    }
                    viewPager.getAdapter().notifyDataSetChanged();  // Recreate if needed
                }
            });
        });
        executor.shutdown();
    }

    // FIXED: Helper to refresh specific fragment based on position to avoid cast errors
    private void refreshSpecificFragment(Fragment fragment, int position) {
        switch (position) {
            case 0:
                if (fragment instanceof AdminCategoryFragment) ((AdminCategoryFragment) fragment).refresh();
                break;
            case 1:
                if (fragment instanceof AdminDishFragment) ((AdminDishFragment) fragment).refresh();
                break;
            case 2:
                if (fragment instanceof TableManagementFragment) ((TableManagementFragment) fragment).refresh();
                break;
            case 3:
                if (fragment instanceof UserManagementFragment) ((UserManagementFragment) fragment).refresh();
                break;
        }
    }

    private void setupViewPager() {
        AdminPagerAdapter adapter = new AdminPagerAdapter(this, categories, dishes, tables, users);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Danh mục");
                    break;
                case 1:
                    tab.setText("Món ăn");
                    break;
                case 2:
                    tab.setText("Bàn Ăn");
                    break;
                case 3:
                    tab.setText("Quản lý User");
                    break;
            }
        }).attach();
    }

    private void setupFab() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    fabAdd.setImageResource(android.R.drawable.ic_input_add);
                } else {
                    fabAdd.setImageResource(android.R.drawable.ic_input_add);
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
                CategoryEntity entity = new CategoryEntity(0, name);
                if (categoryDAO.addCategory(entity) > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all to update display
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

    // FIXED: Helper to refresh all fragments for consistent display update
    private void refreshAllFragments() {
        if (viewPager.getAdapter() != null) {
            for (int i = 0; i < viewPager.getAdapter().getItemCount(); i++) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + i);
                if (fragment != null) {
                    refreshSpecificFragment(fragment, i);
                }
            }
        }
    }

    private void showAddDishDialog() {
        currentImageBase64 = null;
        currentEditingDish = null;
        showDishDialog(null);
    }

    public void showEditDishDialog(Dish dish) {
        currentEditingDish = dish;
        currentImageBase64 = dish.getImageBase64();
        showDishDialog(dish);
    }

    private void showDishDialog(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dish == null ? "Thêm món ăn" : "Sửa món ăn");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_dish_with_image, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPrice = view.findViewById(R.id.et_price);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        ImageView ivDishImage = view.findViewById(R.id.iv_dish_image);
        Button btnChangeImage = view.findViewById(R.id.btn_change_image);
        // Store reference to current ImageView
        currentImageView = ivDishImage;
        // Load categories
        if (categories.isEmpty()) {
            Toast.makeText(this, "Chưa có danh mục nào. Vui lòng thêm danh mục trước.", Toast.LENGTH_LONG).show();
            return;
        }
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
        // Display current image
        updateDishImage(ivDishImage, dish);
        // Image selection event
        btnChangeImage.setOnClickListener(v -> showImagePickerDialog());
        // Pre-fill data if edit
        if (dish != null) {
            etName.setText(dish.getName());
            etPrice.setText(String.valueOf(dish.getPrice()));
            // Select category
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == dish.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        builder.setView(view);
        builder.setPositiveButton(dish == null ? "Thêm" : "Cập nhật", (dialog, which) -> {
            // Validation and save data
            saveDishData(etName, etPrice, spinnerCategory, dish);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateDishImage(ImageView imageView, Dish dish) {
        if (currentImageBase64 != null) {
            // Display temp custom image from variable
            Bitmap bitmap = ImageUtils.base64ToBitmap(currentImageBase64);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        if (dish != null && dish.hasCustomImage()) {
            // Display custom image from dish
            Bitmap bitmap = ImageUtils.base64ToBitmap(dish.getImageBase64());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        if (dish != null && dish.getImageResource() != 0) {
            // Display resource image
            imageView.setImageResource(dish.getImageResource());
        } else {
            // Default image
            imageView.setImageResource(R.drawable.bun_ca);
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh cho món ăn");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_image_picker, null);
        ImageView ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        Button btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        Button btnChooseGallery = view.findViewById(R.id.btn_choose_gallery);
        Button btnUseDefault = view.findViewById(R.id.btn_use_default);
        // Display current image
        updateDialogImage(ivSelectedImage);

        // Hack to reference the dialog in lambdas
        final AlertDialog[] pickerDialogRef = new AlertDialog[1];

        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {  // FIXED: Scoped storage handling
                takePhoto();
            } else {
                needCameraForPicker = true;
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE  // Only for < Android 10
                }, REQUEST_CAMERA_PERMISSION);
            }
            if (pickerDialogRef[0] != null) {
                pickerDialogRef[0].dismiss();
            }
        });

        btnChooseGallery.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+: READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    chooseFromGallery();
                } else {
                    needStorageForPicker = true;
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES
                    }, REQUEST_STORAGE_PERMISSION);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10-12: No permission needed for gallery
                chooseFromGallery();
            } else {
                // < Android 10
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    chooseFromGallery();
                } else {
                    needStorageForPicker = true;
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, REQUEST_STORAGE_PERMISSION);
                }
            }
            if (pickerDialogRef[0] != null) {
                pickerDialogRef[0].dismiss();
            }
        });

        btnUseDefault.setOnClickListener(v -> {
            currentImageBase64 = null;
            if (currentImageView != null) {
                currentImageView.setImageResource(R.drawable.bun_ca);
            }
            Toast.makeText(this, "Đã chọn ảnh mặc định", Toast.LENGTH_SHORT).show();
            if (pickerDialogRef[0] != null) {
                pickerDialogRef[0].dismiss();
            }
        });

        builder.setView(view);
        builder.setNegativeButton("Hủy", null);
        pickerDialogRef[0] = builder.show();
    }

    private void updateDialogImage(ImageView imageView) {
        if (currentImageBase64 != null) {
            Bitmap bitmap = ImageUtils.base64ToBitmap(currentImageBase64);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        if (currentEditingDish != null && currentEditingDish.hasCustomImage()) {
            Bitmap bitmap = ImageUtils.base64ToBitmap(currentEditingDish.getImageBase64());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        imageView.setImageResource(R.drawable.bun_ca);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (needCameraForPicker) {
                    takePhoto();
                    needCameraForPicker = false;
                }
            } else {
                Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                needCameraForPicker = false;
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (needStorageForPicker) {
                    chooseFromGallery();
                    needStorageForPicker = false;
                }
            } else {
                Toast.makeText(this, "Cần quyền truy cập bộ nhớ để chọn ảnh", Toast.LENGTH_SHORT).show();
                needStorageForPicker = false;
            }
        }
    }

    private void takePhoto() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = ImageUtils.createImageFile(this);  // FIXED: Use utils
                if (photoFile != null) {
                    Uri photoUri = ImageUtils.getUriForFile(this, photoFile);  // FIXED: Use utils
                    currentImageUri = photoUri;  // Now declared earlier
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    imageCaptureLauncher.launch(takePictureIntent);  // FIXED: Use launcher
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tạo file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));  // FIXED: Use launcher
    }

    private void handleCapturedImage() {
        if (currentImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentImageUri);
                if (bitmap != null) {
                    // Resize image to reduce size
                    Bitmap resizedBitmap = ImageUtils.resizeBitmap(bitmap, 800, 800);
                    currentImageBase64 = ImageUtils.bitmapToBase64(resizedBitmap);
                    // Update ImageView
                    if (currentImageView != null) {
                        currentImageView.setImageBitmap(resizedBitmap);
                    }
                    Toast.makeText(this, "Đã chụp ảnh thành công", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap != null) {
                // Resize image to reduce size
                Bitmap resizedBitmap = ImageUtils.resizeBitmap(bitmap, 800, 800);
                currentImageBase64 = ImageUtils.bitmapToBase64(resizedBitmap);
                // Update ImageView
                if (currentImageView != null) {
                    currentImageView.setImageBitmap(resizedBitmap);
                }
                Toast.makeText(this, "Đã chọn ảnh thành công", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDishData(EditText etName, EditText etPrice, Spinner spinnerCategory, Dish existingDish) {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int price = Integer.parseInt(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "Giá phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
            Category selectedCat = (Category) spinnerCategory.getSelectedItem();
            if (selectedCat == null) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }
            if (existingDish == null) {
                DishEntity entity = new DishEntity(0, name, price, selectedCat.getId(), R.drawable.bun_ca, currentImageBase64);
                if (dishDAO.addDish(entity) > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all for display update
                    Toast.makeText(this, "Thêm món ăn thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi thêm món ăn", Toast.LENGTH_SHORT).show();
                }
            } else {
                existingDish.setName(name);
                existingDish.setPrice(price);
                existingDish.setCategoryId(selectedCat.getId());
                existingDish.setImageBase64(currentImageBase64);
                DishEntity entity = existingDish.toEntity();
                if (dishDAO.updateDish(entity) > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all for display update
                    Toast.makeText(this, "Cập nhật món ăn thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật món ăn", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    // FIXED: Removed refreshFragment method; use refreshAllFragments instead for better display consistency

    // Method for edit category
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
                CategoryEntity entity = category.toEntity();
                if (categoryDAO.updateCategory(entity) > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all for display update
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

    private void confirmDeleteCategory(Category category) {
        ExecutorService executor = Executors.newSingleThreadExecutor();  // FIXED: Async for DB query
        executor.execute(() -> {
            int dishCount = dishDAO.getDishCountByCategory(category.getId());
            runOnUiThread(() -> {
                if (dishCount > 0) {
                    AlertDialog.Builder cannotDeleteBuilder = new AlertDialog.Builder(this);
                    cannotDeleteBuilder.setTitle("Không thể xóa");
                    cannotDeleteBuilder.setMessage("Không thể xóa danh mục! Danh mục '" + category.getName() + "' còn " + dishCount + " món ăn.");
                    cannotDeleteBuilder.setPositiveButton("OK", null);
                    cannotDeleteBuilder.show();
                    return;
                }
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
                deleteBuilder.setTitle("Xác nhận xóa");
                deleteBuilder.setMessage("Danh mục '" + category.getName() + "' rỗng. Tiếp tục xóa?");
                deleteBuilder.setPositiveButton("Xóa", (dialog, which) -> {
                    if (categoryDAO.deleteCategoryById(category.getId()) > 0) {
                        loadDataAsync();
                        refreshAllFragments();  // FIXED: Refresh all for display update
                        Toast.makeText(this, "Xóa danh mục thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi xóa danh mục", Toast.LENGTH_SHORT).show();
                    }
                });
                deleteBuilder.setNegativeButton("Hủy", null);
                deleteBuilder.show();
            });
        });
        executor.shutdown();
    }

    private void confirmDeleteDish(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa món ăn này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (dishDAO.deleteDish(dish.getId()) > 0) {
                        loadDataAsync();  // FIXED: Reload async
                        refreshAllFragments();  // FIXED: Refresh all for display update
                        Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public void showDeleteDishDialog(Dish dish) {
        confirmDeleteDish(dish);
    }

    private void showAddTableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm bàn");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_table, null);
        EditText etNumber = view.findViewById(R.id.et_number);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_status);
        // Init spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Còn trống", "đang phục vụ"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setSelection(0); // Default "Còn trống"
        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String numberStr = etNumber.getText().toString().trim();
            if (!numberStr.isEmpty()) {
                try {
                    int number = Integer.parseInt(numberStr);
                    if (number <= 0) {
                        Toast.makeText(this, "Số bàn phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String status = spinnerStatus.getSelectedItem().toString();
                    Table table = new Table(0, number, status);
                    TableEntity entity = table.toEntity();
                    if (tableDAO.addTable(entity) > 0) {
                        loadDataAsync();  // FIXED: Reload async
                        refreshAllFragments();  // FIXED: Refresh all for display update
                        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi thêm (có thể số bàn đã tồn tại)", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số bàn không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Số bàn không được rỗng", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    public void showEditTableDialog(Table table) {
        if (table == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bàn để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa bàn " + table.getNumber());
        // Inflate dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_table, null);
        EditText etNumber = view.findViewById(R.id.et_number);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_status);
        // Prefill data
        if (etNumber != null) {
            etNumber.setText(String.valueOf(table.getNumber()));
        } else {
            Log.e(TAG, "et_number not found in layout! Check XML.");
            Toast.makeText(this, "Lỗi layout: Không tìm thấy EditText số bàn", Toast.LENGTH_SHORT).show();
            return;
        }
        // Init spinner
        if (spinnerStatus != null) {
            Log.d(TAG, "Spinner found, current status: " + table.getStatus());
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Còn trống", "đang phục vụ"});
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(statusAdapter);
            spinnerStatus.setVisibility(View.VISIBLE);
            int statusPos = table.getStatus().equals("Còn trống") ? 0 : 1;
            spinnerStatus.setSelection(statusPos);
            Log.d(TAG, "Spinner init success, selection: " + statusPos);
        } else {
            Log.e(TAG, "spinner_status not found in layout! Check dialog_add_table.xml for ID @+id/spinner_status.");
            Toast.makeText(this, "Lỗi layout: Không tìm thấy Spinner trạng thái", Toast.LENGTH_SHORT).show();
            return;
        }
        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String numberStr = etNumber.getText().toString().trim();
            if (numberStr.isEmpty()) {
                Toast.makeText(this, "Số bàn không được rỗng", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int number = Integer.parseInt(numberStr);
                if (number <= 0) {
                    Toast.makeText(this, "Số bàn phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Update table
                table.setNumber(number);
                table.setStatus(spinnerStatus.getSelectedItem().toString());
                Log.d(TAG, "Updated status to: " + table.getStatus());
                TableEntity entity = table.toEntity();
                int result = tableDAO.updateTable(entity);
                if (result > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all for display update
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật (có thể số bàn đã tồn tại)", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số bàn không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.setNeutralButton("Xóa", (dialog, which) -> confirmDeleteTable(table));
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.d(TAG, "Dialog shown for table " + table.getNumber());
    }

    private void confirmDeleteTable(Table table) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa bàn " + table.getNumber() + " sẽ ảnh hưởng đến orders liên quan. Tiếp tục?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = tableDAO.deleteTable(table.getId());
                    if (result > 0) {
                        loadDataAsync();  // FIXED: Reload async
                        refreshAllFragments();  // FIXED: Refresh all for display update
                        Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi xóa (có thể có orders liên quan)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm User");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = view.findViewById(R.id.et_username);
        EditText etPassword = view.findViewById(R.id.et_password);
        Spinner spinnerRole = view.findViewById(R.id.spinner_role);
        // Init spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"admin", "employee"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        builder.setView(view);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            // FIXED: Check duplicate username - now boolean return from DAO
            if (userDAO.isUsernameExists(username)) {
                Toast.makeText(this, "Username '" + username + "' đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }
            User newUser = new User(0, username, password, role);
            // Hash password if production (add hash code here)
            try {
                UserEntity entity = newUser.toEntity();
                long id = userDAO.addUser(entity);
                if (id > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all for display update
                    Toast.makeText(this, "Thêm user thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Lỗi thêm user", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi thêm user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Add user error", e);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    public void showEditUserDialog(User user) {
        if (user == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy user để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa User: " + user.getUsername());
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        EditText etUsername = view.findViewById(R.id.et_username);
        EditText etPassword = view.findViewById(R.id.et_password);
        Spinner spinnerRole = view.findViewById(R.id.spinner_role);
        // Prefill data
        etUsername.setText(user.getUsername());
        etPassword.setText("");
        // No prefill password (security)
        // Init spinner for role
        String[] roles = {"admin", "employee"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
        // Set selection based on current role
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(user.getRole())) {
                spinnerRole.setSelection(i);
                break;
            }
        }
        builder.setView(view);
        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            try {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String role = spinnerRole.getSelectedItem().toString();
                if (username.isEmpty()) {
                    // Password can be empty if not changing
                    Toast.makeText(this, "Vui lòng nhập username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!username.equals(user.getUsername()) && userDAO.isUsernameExists(username)) {
                    // FIXED: Direct boolean check, no > 0
                    // Check duplicate if changing username
                    Toast.makeText(this, "Username '" + username + "' đã tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Keep old password if empty
                User editedUser = new User(user.getId(), username, password.isEmpty() ? user.getPassword() : password, role);
                // Hash password if changing (add hash code here)
                UserEntity entity = editedUser.toEntity();
                int result = userDAO.updateUser(entity);
                if (result > 0) {
                    loadDataAsync();  // FIXED: Reload async
                    refreshAllFragments();  // FIXED: Refresh all for display update
                    Toast.makeText(this, "Cập nhật user thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật user", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Edit user error", e);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.setNeutralButton("Xóa", (dialog, which) -> confirmDeleteUser(user));
        builder.show();
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa user '" + user.getUsername() + "' sẽ ảnh hưởng đến login. Tiếp tục? (Không thể khôi phục)")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int result = userDAO.deleteUser(user.getId());
                    if (result > 0) {
                        loadDataAsync();  // FIXED: Reload async
                        refreshAllFragments();  // FIXED: Refresh all for display update
                        Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi xóa (có thể đang login)", Toast.LENGTH_SHORT).show();
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
        loadDataAsync();  // FIXED: Reload async for fresh display
    }
}