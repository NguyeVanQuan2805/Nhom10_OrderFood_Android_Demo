package com.example.neworderfood.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.neworderfood.R;
import com.example.neworderfood.dao.CategoryDAO;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.dao.InvoiceDAO;
import com.example.neworderfood.dao.OrderDAO;
import com.example.neworderfood.dao.OrderItemDAO;
import com.example.neworderfood.dao.TableDAO;
import com.example.neworderfood.dao.UserDAO; // New
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.Invoice;
import com.example.neworderfood.models.Order;
import com.example.neworderfood.models.OrderItem;
import com.example.neworderfood.models.User; // New

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CukCuk.db";
    public static final int DATABASE_VERSION = 8; // Increased for image column

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
    public static final String COL_DISH_IMAGE_BASE64 = "image_base64";

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

    // columns table
    public static final String TABLE_TABLES = "tables";  // NEW
    public static final String COL_TABLE_ID = "id";
    public static final String COL_TABLE_NUMBER = "number";
    public static final String COL_TABLE_STATUS = "status";

    public final Context mContext;
    public DishDAO dishDAO;
    public OrderDAO orderDAO;
    public OrderItemDAO orderItemDAO;
    public CategoryDAO categoryDAO;
    public UserDAO userDAO; // New
    public TableDAO tableDAO;
    private InvoiceDAO invoiceDAO;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users first (before categories and dishes)
        String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +  // THÊM IF NOT EXISTS
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_USERNAME + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_ROLE + " TEXT)";
        db.execSQL(createUsers);

        // Tạo bảng Categories
        String createCategories = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" +  // THÊM IF NOT EXISTS
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT UNIQUE)";
        db.execSQL(createCategories);

        // Tạo bảng Dishes (với image column)
        String createDishes = "CREATE TABLE IF NOT EXISTS " + TABLE_DISHES + " (" +  // THÊM IF NOT EXISTS
                COL_DISH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DISH_NAME + " TEXT, " +
                COL_DISH_PRICE + " INTEGER, " +
                COL_DISH_CATEGORY + " INTEGER, " +
                COL_DISH_IMAGE + " INTEGER, " +
                COL_DISH_IMAGE_BASE64 + " TEXT, " +
                "FOREIGN KEY(" + COL_DISH_CATEGORY + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CAT_ID + "))";
        db.execSQL(createDishes);

        // Tạo bảng Orders
        String createOrders = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + " (" +  // THÊM IF NOT EXISTS
                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_TABLE + " INTEGER, " +
                COL_ORDER_TOTAL + " INTEGER, " +
                COL_ORDER_STATUS + " TEXT, " +
                COL_ORDER_CREATED + " TEXT)";
        db.execSQL(createOrders);

        // Tạo bảng OrderItems
        String createItems = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_ITEMS + " (" +  // THÊM IF NOT EXISTS
                COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_ORDER_ID + " INTEGER, " +
                COL_ITEM_DISH_ID + " INTEGER, " +
                COL_ITEM_QUANTITY + " INTEGER, " +
                COL_ITEM_NOTES + " TEXT, " +
                COL_ITEM_DISCOUNT + " INTEGER, " +
                "FOREIGN KEY(" + COL_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COL_ORDER_ID + "), " +
                "FOREIGN KEY(" + COL_ITEM_DISH_ID + ") REFERENCES " + TABLE_DISHES + "(" + COL_DISH_ID + "))";
        db.execSQL(createItems);

        // Tạo bảng Tables
        String createTablesTable = "CREATE TABLE IF NOT EXISTS tables (" +  // THÊM IF NOT EXISTS
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "number INTEGER NOT NULL UNIQUE, " +
                "status TEXT DEFAULT 'Còn trống')";
        db.execSQL(createTablesTable);

        // THÊM: Tạo bảng Invoices (mới)
        String createInvoices = "CREATE TABLE IF NOT EXISTS invoices (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "table_number INTEGER, " +
                "total_amount INTEGER, " +
                "change_amount INTEGER)";
        db.execSQL(createInvoices);

        // THÊM: Tạo bảng InvoiceItems
        String createInvoiceItems = "CREATE TABLE IF NOT EXISTS invoice_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "invoice_id INTEGER, " +
                "dish_id INTEGER, " +
                "quantity INTEGER, " +
                "notes TEXT, " +
                "FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE CASCADE)";
        db.execSQL(createInvoiceItems);

        // Insert sample data (chỉ nếu bảng rỗng)
        insertSampleUsers(db);
        insertSampleCategories(db);
        insertSampleDishes(db);
        insertSampleTables(db);
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

    // SỬA TRONG DatabaseHelper.java - phần insertSampleDishes
    // TRONG DatabaseHelper.java - sửa insertSampleDishes
    private void insertSampleDishes(SQLiteDatabase db) {
        String[][] dishData = {
                {"Bún cá", "30000", "1", "bun_ca"},
                {"Bánh đa cua", "30000", "1", "banh_da_cua"},
                {"Bún riêu cua", "30000", "1", "bun_rieu_cua"},
                {"Bún cá giò", "35000", "1", "bun_ca_gio"},
                {"Bún cá mọc", "35000", "1", "bun_ca_moc"},
                {"Bún cá bò", "35000", "1", "bun_ca_bo"},
                {"Quẩy", "5000", "3", "quay"},
                {"Rau thơm", "10000", "3", "rau_thom"},
                {"Coca", "10000", "2", "coca"},
                {"Nước cam", "15000", "2", "nuoc_cam"}
        };

        for (String[] data : dishData) {
            ContentValues values = new ContentValues();
            values.put(COL_DISH_NAME, data[0]);
            values.put(COL_DISH_PRICE, Integer.parseInt(data[1]));
            values.put(COL_DISH_CATEGORY, Integer.parseInt(data[2]));

            // THÊM LOG ĐỂ DEBUG
            int imageResId = mContext.getResources().getIdentifier(data[3], "drawable", mContext.getPackageName());
            Log.d("DatabaseHelper", "Dish: " + data[0] + ", Resource: " + data[3] + ", ID: " + imageResId);

            if (imageResId > 0) {
                values.put(COL_DISH_IMAGE, imageResId);
            } else {
                // Nếu không tìm thấy, sử dụng resource khác nhau cho từng category
                int fallbackImage = getFallbackImage(Integer.parseInt(data[2]));
                values.put(COL_DISH_IMAGE, fallbackImage);
                Log.w("DatabaseHelper", "Fallback image for: " + data[0] + " -> " + fallbackImage);
            }

            db.insert(TABLE_DISHES, null, values);
        }
    }

    private void insertSampleTables(SQLiteDatabase db) {
        int[] sampleNumbers = {1, 2, 3, 4, 5};
        for (int num : sampleNumbers) {
            Cursor cursor = db.query("tables", new String[]{"id"}, "number = ?", new String[]{String.valueOf(num)}, null, null, null);
            if (cursor.getCount() == 0) {  // Chưa tồn tại
                ContentValues values = new ContentValues();
                values.put(COL_TABLE_NUMBER, num);
                values.put(COL_TABLE_STATUS, "Còn trống");
                db.insert(TABLE_TABLES, null, values);
            }
            cursor.close();
        }
    }

    // Thêm phương thức fallback image theo category
    private int getFallbackImage(int categoryId) {
        switch (categoryId) {
            case 1: // Bún cá
                return R.drawable.bun_ca;
            case 2: // Đồ uống
                return R.drawable.nuoc_cam; // hoặc R.drawable.nuoc_cam
            case 3: // Khác
                return R.drawable.quay;
            default:
                return R.drawable.bun_ca;
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 8) {  // THÊM: Tạo bảng invoices nếu version < 8
            String createInvoices = "CREATE TABLE invoices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT, " +
                    "table_number INTEGER, " +
                    "total_amount INTEGER, " +
                    "change_amount INTEGER)";
            db.execSQL(createInvoices);

            String createInvoiceItems = "CREATE TABLE invoice_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "invoice_id INTEGER, " +
                    "dish_id INTEGER, " +
                    "quantity INTEGER, " +
                    "notes TEXT, " +
                    "FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE CASCADE)";
            db.execSQL(createInvoiceItems);
        }
        if (oldVersion < 6) {
            // Thêm cột image_base64
            db.execSQL("ALTER TABLE " + TABLE_DISHES + " ADD COLUMN " + COL_DISH_IMAGE_BASE64 + " TEXT");
        }
        if (oldVersion < 4) {
            // Add image column
            db.execSQL("ALTER TABLE " + TABLE_DISHES + " ADD COLUMN " + COL_DISH_IMAGE + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            // Add users table
            String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +  // SỬA: IF NOT EXISTS +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME + " TEXT UNIQUE, " +
                    COL_USER_PASSWORD + " TEXT, " +
                    COL_USER_ROLE + " TEXT)";
            db.execSQL(createUsers);
            insertSampleUsers(db);
        }
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS tables");  // Drop nếu cần
            // Gọi lại onCreate để tạo mới
            onCreate(db);
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
    // Thêm vào DishDAO hoặc DatabaseHelper
    public void debugDishImages() {
        List<Dish> dishes = getAllDishes();
        for (Dish dish : dishes) {
            String resourceName = "";
            try {
                resourceName = mContext.getResources().getResourceName(dish.getImageResource());
            } catch (Exception e) {
                resourceName = "Unknown (" + dish.getImageResource() + ")";
            }
            Log.d("DishDebug", "Dish: " + dish.getName() + " -> Image: " + resourceName);
        }
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
    public TableDAO getTableDAO() {
        if (tableDAO == null) tableDAO = new TableDAO(mContext);
        return tableDAO;
    }
    public void addTable(com.example.neworderfood.models.Table table) { getTableDAO().addTable(table); }
    public int updateTable(com.example.neworderfood.models.Table table) {
        return getTableDAO().updateTable(table);  // SỬA: Return int từ DAO
    }
    public int deleteTable(int id) { return getTableDAO().deleteTable(id); }
    public List<com.example.neworderfood.models.Table> getAllTables() { return getTableDAO().getAllTables(); }
    public void updateTableStatus(int tableId, String status) { getTableDAO().updateTableStatus(tableId, status); }

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
    public List<User> getAllUsers() {
        return getUserDAO().getAllUsers();  // THÊM method này vào UserDAO
    }
    // Thêm vào DatabaseHelper
    public int updateUser(User user) { return getUserDAO().updateUser(user); }
    public int deleteUser(int id) { return getUserDAO().deleteUser(id); }
    // New delegates for User
    public User getUserByUsernameAndPassword(String username, String password) {
        return getUserDAO().getUserByUsernameAndPassword(username, password);
    }

    public InvoiceDAO getInvoiceDAO() {
        if (invoiceDAO == null) invoiceDAO = new InvoiceDAO(mContext);
        return invoiceDAO;
    }
    public long addInvoice(Invoice invoice) { return getInvoiceDAO().addInvoice(invoice); }
    public List<Invoice> getAllInvoices() { return getInvoiceDAO().getAllInvoices(); }
    public List<OrderItem> getInvoiceItemsByInvoiceId(int invoiceId) { return getInvoiceDAO().getInvoiceItemsByInvoiceId(invoiceId); }
}