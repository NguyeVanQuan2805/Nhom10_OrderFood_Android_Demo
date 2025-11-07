package com.example.neworderfood.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.neworderfood.dao.CategoryDAO;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.dao.OrderDAO;
import com.example.neworderfood.dao.OrderItemDAO;
import com.example.neworderfood.dao.UserDAO; // New
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.Order;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.models.User; // New

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CukCuk.db";
    public static final int DATABASE_VERSION = 4;  // Increased for image column

    // Tables
    public static final String TABLE_DISHES = "dishes";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_USERS = "users"; // New table

    // Dish columns
    public static final String COL_DISH_ID = "id";
    public static final String COL_DISH_NAME = "name";
    public static final String COL_DISH_PRICE = "price";
    public static final String COL_DISH_CATEGORY = "category_id";
    public static final String COL_DISH_IMAGE = "image_resource";  // NEW

    // Order columns
    public static final String COL_ORDER_ID = "id";
    public static final String COL_ORDER_TABLE = "table_number";
    public static final String COL_ORDER_TOTAL = "total_amount";
    public static final String COL_ORDER_STATUS = "status";
    public static final String COL_ORDER_CREATED = "created_at";

    // OrderItem columns
    public static final String COL_ITEM_ID = "id";
    public static final String COL_ITEM_ORDER_ID = "order_id";
    public static final String COL_ITEM_DISH_ID = "dish_id";
    public static final String COL_ITEM_QUANTITY = "quantity";
    public static final String COL_ITEM_NOTES = "notes";
    public static final String COL_ITEM_DISCOUNT = "discount";

    // Category columns
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";

    // User columns - New
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROLE = "role";

    public final Context mContext;
    public DishDAO dishDAO;
    public OrderDAO orderDAO;
    public OrderItemDAO orderItemDAO;
    public CategoryDAO categoryDAO;
    public UserDAO userDAO; // New

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users first (before categories and dishes)
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_USERNAME + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_ROLE + " TEXT)";
        db.execSQL(createUsers);

        // Tạo bảng Categories
        String createCategories = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT UNIQUE)";
        db.execSQL(createCategories);

        // Tạo bảng Dishes (với image column)
        String createDishes = "CREATE TABLE " + TABLE_DISHES + " (" +
                COL_DISH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DISH_NAME + " TEXT, " +
                COL_DISH_PRICE + " INTEGER, " +
                COL_DISH_CATEGORY + " INTEGER, " +
                COL_DISH_IMAGE + " INTEGER, " +
                "FOREIGN KEY(" + COL_DISH_CATEGORY + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CAT_ID + "))";
        db.execSQL(createDishes);

        // Tạo bảng Orders
        String createOrders = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_TABLE + " INTEGER, " +
                COL_ORDER_TOTAL + " INTEGER, " +
                COL_ORDER_STATUS + " TEXT, " +
                COL_ORDER_CREATED + " TEXT)";
        db.execSQL(createOrders);

        // Tạo bảng OrderItems
        String createItems = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_ORDER_ID + " INTEGER, " +
                COL_ITEM_DISH_ID + " INTEGER, " +
                COL_ITEM_QUANTITY + " INTEGER, " +
                COL_ITEM_NOTES + " TEXT, " +
                COL_ITEM_DISCOUNT + " INTEGER, " +
                "FOREIGN KEY(" + COL_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COL_ORDER_ID + "), " +
                "FOREIGN KEY(" + COL_ITEM_DISH_ID + ") REFERENCES " + TABLE_DISHES + "(" + COL_DISH_ID + "))";
        db.execSQL(createItems);

        // Insert sample data
        insertSampleUsers(db);
        insertSampleCategories(db);
        insertSampleDishes(db);
    }

    private void insertSampleUsers(SQLiteDatabase db) {
        // Sample users: admin/123 (admin), employee/456 (employee)
        User[] sampleUsers = {
                new User(0, "admin", "123", "admin"),
                new User(0, "employee", "456", "employee")
        };
        for (User user : sampleUsers) {
            ContentValues values = new ContentValues();
            values.put(COL_USER_USERNAME, user.getUsername());
            values.put(COL_USER_PASSWORD, user.getPassword());
            values.put(COL_USER_ROLE, user.getRole());
            db.insert(TABLE_USERS, null, values);
        }
    }

    private void insertSampleCategories(SQLiteDatabase db) {
        String[] sampleCats = {"Bún cá", "Đồ uống", "Khác"};
        for (String catName : sampleCats) {
            ContentValues values = new ContentValues();
            values.put(COL_CAT_NAME, catName);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    private void insertSampleDishes(SQLiteDatabase db) {
        String[][] dishData = {
                {"Bún cá", "30000", "Bún cá", "bun_ca"},
                {"Bánh đa cua", "30000", "Bún cá", "banh_da_cua"},
                {"Bún riêu cua", "30000", "Bún cá", "bun_rieu_cua"},
                {"Bún cá giò", "35000", "Bún cá", "bun_ca_gio"},
                {"Bún cá mọc", "35000", "Bún cá", "bun_ca_moc"},
                {"Bún cá bò", "35000", "Bún cá", "bun_ca_bo"},
                {"Quẩy", "5000", "Khác", "quay"},
                {"Rau thơm", "10000", "Khác", "rau_thom"},
                {"Coca", "10000", "Đồ uống", "coca"},
                {"Nước cam", "15000", "Đồ uống", "nuoc_cam"}
        };

        for (String[] data : dishData) {
            Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COL_CAT_ID},
                    COL_CAT_NAME + " = ?", new String[]{data[2]}, null, null, null);
            if (cursor.moveToFirst()) {
                int catId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_ID));
                ContentValues values = new ContentValues();
                values.put(COL_DISH_NAME, data[0]);
                values.put(COL_DISH_PRICE, Integer.parseInt(data[1]));
                values.put(COL_DISH_CATEGORY, catId);
                // SỬA: Sử dụng mContext để lấy resource ID (nếu file drawable tồn tại, nếu không = 0)
                int imageResId = mContext.getResources().getIdentifier(data[3], "drawable", mContext.getPackageName());
                values.put(COL_DISH_IMAGE, imageResId > 0 ? imageResId : 0);  // 0 nếu không tìm thấy
                db.insert(TABLE_DISHES, null, values);
            }
            cursor.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Add image column
            db.execSQL("ALTER TABLE " + TABLE_DISHES + " ADD COLUMN " + COL_DISH_IMAGE + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            // Add users table
            db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME + " TEXT UNIQUE, " +
                    COL_USER_PASSWORD + " TEXT, " +
                    COL_USER_ROLE + " TEXT)");
            insertSampleUsers(db);
        }
        if (oldVersion < 2) {
            // Previous migrations for categories
            db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CAT_NAME + " TEXT UNIQUE)");
            insertSampleCategories(db);
            // Update dishes if needed...
        }
        // Drop and recreate if necessary
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Lazy initialization cho DAO
    public DishDAO getDishDAO() {
        if (dishDAO == null) dishDAO = new DishDAO(mContext);
        return dishDAO;
    }

    public OrderDAO getOrderDAO() {
        if (orderDAO == null) orderDAO = new OrderDAO(mContext);
        return orderDAO;
    }

    public OrderItemDAO getOrderItemDAO() {
        if (orderItemDAO == null) orderItemDAO = new OrderItemDAO(mContext);
        return orderItemDAO;
    }

    public CategoryDAO getCategoryDAO() {
        if (categoryDAO == null) categoryDAO = new CategoryDAO(mContext);
        return categoryDAO;
    }

    public UserDAO getUserDAO() { // New
        if (userDAO == null) userDAO = new UserDAO(mContext);
        return userDAO;
    }

    // Delegate methods (unchanged + new for User)
    public long addOrder(Order order) { return getOrderDAO().addOrder(order); }
    public long addOrderItem(OrderItem item) { return getOrderItemDAO().addOrderItem(item); }
    public List<Order> getAllOrders() { return getOrderDAO().getAllOrders(); }
    public void deleteOrderById(int orderId) { getOrderDAO().deleteOrderById(orderId); }
    public List<Dish> getDishesByCategory(int categoryId) { return getDishDAO().getDishesByCategory(categoryId); }
    public List<Dish> getAllDishes() { return getDishDAO().getAllDishes(); }
    public Dish getDishById(int id) { return getDishDAO().getDishById(id); }
    public void updateOrderStatus(int orderId, String status) { getOrderDAO().updateOrderStatus(orderId, status); }
    public List<OrderItem> getOrderItemsByOrderId(int orderId) { return getOrderItemDAO().getOrderItemsByOrderId(orderId); }
    public void deleteOrderItemsByOrderId(int orderId) { getOrderItemDAO().deleteOrderItemsByOrderId(orderId); }
    public List<Category> getAllCategories() { return getCategoryDAO().getAllCategories(); }
    public Category getCategoryById(int id) { return getCategoryDAO().getCategoryById(id); }

    // New delegates for User
    public User getUserByUsernameAndPassword(String username, String password) {
        return getUserDAO().getUserByUsernameAndPassword(username, password);
    }
}