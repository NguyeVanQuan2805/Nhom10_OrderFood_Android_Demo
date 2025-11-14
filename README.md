OrderFood Restaurant Management App

Giới thiệu

OrderFood là ứng dụng quản lý nhà hàng đơn giản, được xây dựng bằng Android Studio. Ứng dụng hỗ trợ quản lý danh mục món ăn, bàn ăn, user, order, hóa đơn và thanh toán. Phù hợp cho nhà hàng nhỏ, với giao diện thân thiện và tích hợp cơ sở dữ liệu Room SQLite.
Dự án được phát triển như một ứng dụng demo, tập trung vào các tính năng cốt lõi như đặt món, quản lý admin, và xử lý thanh toán. Ứng dụng hỗ trợ cả admin (quản lý) và employee (nhân viên phục vụ).
Tính năng chính

Quản lý Admin:
Thêm/sửa/xóa danh mục món ăn (Category).
Thêm/sửa/xóa món ăn (Dish) với hỗ trợ upload ảnh custom (camera/gallery, lưu Base64).
Quản lý bàn ăn (Table) với trạng thái (Còn trống/Đang phục vụ).
Quản lý user (Admin/Employee) với login/logout.

Giao diện Menu:
Duyệt món theo danh mục (TabLayout).
Tìm kiếm món ăn.
Chi tiết món với tùy chọn rau (cho danh mục Bún).
Thêm vào order với số lượng, ghi chú, giảm giá.

Quản lý Order:

Tạo order mới hoặc thêm món vào order cũ.
Tóm tắt order trước khi lưu.
Hiển thị danh sách orders đang phục vụ.

Thanh toán & Hóa đơn:
Thu tiền với các mệnh giá cố định (35k, 50k, ...).
Tính tiền thừa tự động.
Tạo hóa đơn (Invoice) sau thanh toán, lưu chi tiết items.
Xem danh sách hóa đơn cũ.

Trạng thái bàn: Hiển thị bàn trống/đang dùng.
Login: Đăng nhập với role-based access (Admin → Quản lý, Employee → Menu/Order).

Công nghệ sử dụng

Ngôn ngữ: Java (Android).
UI Framework: AndroidX, Material Design 3 (TabLayout, RecyclerView, FloatingActionButton).
Database: Room (SQLite) với Entity, Dao, LiveData (sync/async queries).
Hình ảnh: Base64 encoding cho ảnh custom (ImageUtils), fallback resource drawable.
Permissions: Camera, Storage (cho upload ảnh).
Khác: ExecutorService cho async DB ops, SharedPreferences cho session login.
Dependencies chính (trong build.gradle (Module: app)):textimplementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.viewpager2:viewpager2:1.0.0'

Yêu cầu hệ thống

Android Studio: Hedgehog (2023.1.1) hoặc mới hơn.
Min SDK: API 24 (Android 7.0).
Target SDK: API 34 (Android 14).
Device/Emulator: Android 7.0+, hỗ trợ camera (cho test upload ảnh).

Hướng dẫn cài đặt & chạy

Clone repo:textgit clone https://github.com/your-username/cukcuk-restaurant-app.git
cd cukcuk-restaurant-app
Mở project:
Mở bằng Android Studio: File > Open > Chọn thư mục project.
Sync Gradle (nếu prompt).

Cấu hình:
Thêm quyền trong AndroidManifest.xml (đã có sẵn):xml<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" /> <!-- Android 13+ -->
Tạo file drawable nếu thiếu (bun_ca.png, quay.png, nuoc_cam.png) trong res/drawable.

Build & Run:
Build: Build > Make Project.
Chạy: Chọn device/emulator > Run > Run 'app'.
Sample data: DB tự tạo users (admin/123), categories, dishes, tables khi chạy lần đầu.

Test luồng:

Login: admin/123 → MainActivity (orders).
Đặt order: Add Order → Chọn bàn → Menu → Thêm món → Lưu → Thu tiền.
Admin: Logout → Login admin → Quản lý (tabs: Danh mục, Món ăn, Bàn, User).

Đóng góp

Fork repo → Tạo branch → Commit → Pull Request.
Báo issue: Mô tả lỗi + logcat + steps to reproduce.

Cảm ơn bạn đã quan tâm đến dự án! Nếu có câu hỏi, mở issue hoặc liên hệ nguyenvanquan0528@gmail.
