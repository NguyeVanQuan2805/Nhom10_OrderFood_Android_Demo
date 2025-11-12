package com.example.neworderfood.activitis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.neworderfood.R;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.OrderItem;

public class DishDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dish_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);  // THÊM ID toolbar vào XML (xem bước 3)
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chi tiết món");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // THÊM: Nút quay về (mũi tên back)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        Dish dish = (Dish) getIntent().getSerializableExtra("dish");
        int categoryId = getIntent().getIntExtra("categoryId", 0);  // THÊM: Lấy categoryId, default 0 nếu không có

        TextView tvName = findViewById(R.id.tv_dish_name);
        tvName.setText(dish.getName());
        TextView tvPrice = findViewById(R.id.tv_price);
        tvPrice.setText(String.format("%,dđ", dish.getPrice()));

        EditText etQuantity = findViewById(R.id.et_quantity);
        EditText etNotes = findViewById(R.id.et_notes);
        Button btnAdd = findViewById(R.id.btn_add);

        // THÊM: Group CheckBox cho rau
        CheckBox cbRaTran = findViewById(R.id.cb_ra_tran);
        CheckBox cbRaCai = findViewById(R.id.cb_ra_cai);
        CheckBox cbRaNgot = findViewById(R.id.cb_ra_ngot);
        TextView tvRauLabel = findViewById(R.id.tv_rau_label);

        // THÊM: Ẩn/hiện phần chọn rau dựa trên categoryId (1 = Bún cá)
        boolean showRau = (categoryId == 1);  // Chỉ hiện cho danh mục Bún
        if (showRau) {
            // Hiện CheckBox và label
            cbRaTran.setVisibility(View.VISIBLE);
            cbRaCai.setVisibility(View.VISIBLE);
            cbRaNgot.setVisibility(View.VISIBLE);
            if (tvRauLabel != null) tvRauLabel.setVisibility(View.VISIBLE);

            // Set listener cho CheckBox (giữ nguyên code cũ)
            cbRaTran.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateNotes(etNotes, isChecked, "Rau tần");
            });
            cbRaCai.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateNotes(etNotes, isChecked, "Rau cải");
            });
            cbRaNgot.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateNotes(etNotes, isChecked, "Rau ngót");
            });
        } else {
            // Ẩn CheckBox và label
            cbRaTran.setVisibility(View.GONE);
            cbRaCai.setVisibility(View.GONE);
            cbRaNgot.setVisibility(View.GONE);
            if (tvRauLabel != null) tvRauLabel.setVisibility(View.GONE);
        }

        btnAdd.setOnClickListener(v -> {
            String quantityStr = etQuantity.getText().toString();
            if (quantityStr.isEmpty()) {
                quantityStr = "1";
            }
            int quantity = Integer.parseInt(quantityStr);
            OrderItem item = new OrderItem(0, 0, dish.getId(), quantity, etNotes.getText().toString());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("item", item);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private void updateNotes(EditText etNotes, boolean isChecked, String rauType) {
        String currentNotes = etNotes.getText().toString();
        if (isChecked) {
            etNotes.setText(currentNotes.isEmpty() ? rauType : currentNotes + ", " + rauType);
        } else {
            // Remove if unchecked (simple, no exact match check for demo)
            etNotes.setText(currentNotes.replace(", " + rauType, "").replace(rauType, ""));
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // Quay về MenuActivity
        return true;
    }
}