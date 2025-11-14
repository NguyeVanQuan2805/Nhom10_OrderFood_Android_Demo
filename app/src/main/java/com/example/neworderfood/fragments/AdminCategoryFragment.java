package com.example.neworderfood.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.neworderfood.R;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neworderfood.adapters.AdminCategoryAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.CategoryDao;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.room.entities.CategoryEntity;
import com.example.neworderfood.activities.AdminManagementActivity;  // SỬA: Import đúng package

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminCategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private AppDatabase db;
    private CategoryDao categoryDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_category, container, false);

        db = AppDatabase.getInstance(getContext());
        categoryDAO = db.categoryDao();

        recyclerView = view.findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        adapter = new AdminCategoryAdapter(categories, category -> {
            if (getActivity() instanceof AdminManagementActivity) {  // SỬA: Import và package đúng
                ((AdminManagementActivity) getActivity()).showEditCategoryDialog(category);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // THÊM VÀO AdminCategoryFragment.java và AdminDishFragment.java
    private void loadData() {
        try {
            categories.clear();
            // USE SYNC
            List<CategoryEntity> entities = categoryDAO.getAllCategoriesSync();
            if (entities != null) {
                categories.addAll(Category.fromEntities(entities));
            }
            if (adapter != null) adapter.updateCategories(categories);  // Use update method in adapter
        } catch (Exception e) {
            Log.e("AdminCategoryFragment", "Error loading categories", e);
        }
    }

    // Refresh method if needed
    public void refresh() {
        loadData();
    }
}