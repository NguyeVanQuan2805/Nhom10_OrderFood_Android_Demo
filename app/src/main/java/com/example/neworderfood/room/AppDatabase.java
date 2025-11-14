package com.example.neworderfood.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.neworderfood.R;
import com.example.neworderfood.room.daos.CategoryDao;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.room.daos.InvoiceDao;
import com.example.neworderfood.room.daos.InvoiceItemDao;
import com.example.neworderfood.room.daos.OrderDao;
import com.example.neworderfood.room.daos.OrderItemDao;
import com.example.neworderfood.room.daos.TableDao;
import com.example.neworderfood.room.daos.UserDao;
import com.example.neworderfood.room.entities.CategoryEntity;
import com.example.neworderfood.room.entities.DishEntity;
import com.example.neworderfood.room.entities.InvoiceEntity;
import com.example.neworderfood.room.entities.InvoiceItemEntity;
import com.example.neworderfood.room.entities.OrderEntity;
import com.example.neworderfood.room.entities.OrderItemEntity;
import com.example.neworderfood.room.entities.TableEntity;
import com.example.neworderfood.room.entities.UserEntity;

@Database(entities = {
        CategoryEntity.class,
        DishEntity.class,
        TableEntity.class,
        UserEntity.class,
        OrderEntity.class,
        OrderItemEntity.class,
        InvoiceEntity.class,
        InvoiceItemEntity.class
}, version = 10, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // Abstract DAOs
    public abstract CategoryDao categoryDao();
    public abstract DishDao dishDao();
    public abstract TableDao tableDao();
    public abstract UserDao userDao();
    public abstract OrderDao orderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract InvoiceDao invoiceDao();
    public abstract InvoiceItemDao invoiceItemDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "CukCuk.db"
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration() // THÊM DÒNG NÀY
                            .addCallback(S_ROOM_CALLBACK)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("PRAGMA foreign_keys = ON;");  // Enable FK globally
            // Or drop/recreate if test: database.execSQL("DROP TABLE IF EXISTS invoice_items");
        }
    };
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Drop old tables (Room sẽ recreate)
            database.execSQL("DROP TABLE IF EXISTS categories");
            database.execSQL("DROP TABLE IF EXISTS dishes");
            database.execSQL("DROP TABLE IF EXISTS tables");
            database.execSQL("DROP TABLE IF EXISTS users");
            database.execSQL("DROP TABLE IF EXISTS orders");
            database.execSQL("DROP TABLE IF EXISTS order_items");
            database.execSQL("DROP TABLE IF EXISTS invoices");
            database.execSQL("DROP TABLE IF EXISTS invoice_items");
        }
    };

    // SỬA: Callback dùng SupportSQLiteDatabase (Room 2.4+)
    private static final RoomDatabase.Callback S_ROOM_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Insert sample users
            db.execSQL("INSERT OR IGNORE INTO users (username, password, role) VALUES ('admin', '123', 'admin')");
            db.execSQL("INSERT OR IGNORE INTO users (username, password, role) VALUES ('employee', '456', 'employee')");

            // Insert sample categories
            db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES ('Bún cá')");
            db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES ('Đồ uống')");
            db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES ('Khác')");

            // Insert sample dishes (sử dụng 0 cho imageResource nếu không có ID; fallback trong adapter)
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Bún cá', 30000, 1, " + R.drawable.bun_ca + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Bánh đa cua', 30000, 1, " + R.drawable.banh_da_cua + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Bún riêu cua', 30000, 1, " + R.drawable.bun_rieu_cua + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Bún cá giò', 35000, 1, " + R.drawable.bun_ca_gio + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Bún cá mọc', 35000, 1, " + R.drawable.bun_ca_moc + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Bún cá bò', 35000, 1, " + R.drawable.bun_ca_bo + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Quẩy', 5000, 3, " + R.drawable.quay + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Rau thơm', 10000, 3, " + R.drawable.rau_thom + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Sinh tố xoài', 10000, 2, " + R.drawable.sinh_to_xoai + ", Null)");
            db.execSQL("INSERT OR IGNORE INTO dishes (name, price, categoryId, imageResource, imageBase64) VALUES ('Nước cam', 15000, 2, " + R.drawable.nuoc_cam + ", Null)");

            // Insert sample tables
            for (int i = 1; i <= 5; i++) {
                db.execSQL("INSERT OR IGNORE INTO tables (number, status) VALUES (" + i + ", 'Còn trống')");
            }
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Optional: Log khi DB mở (dùng Log.d nếu cần)
        }
    };
}