package com.example.neworderfood.fragment;
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
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.CategoryDAO;
import com.example.neworderfood.models.Category;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminCategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private CategoryDAO categoryDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_category, container, false);
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        categoryDAO = dbHelper.getCategoryDAO();
        recyclerView = view.findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadData();
        adapter = new AdminCategoryAdapter(categories, category -> {
            if (getActivity() instanceof com.example.neworderfood.activitis.AdminManagementActivity) {
                ((com.example.neworderfood.activitis.AdminManagementActivity) getActivity()).showEditCategoryDialog(category);
            }
        });
        recyclerView.setAdapter(adapter);
        return view;
    }

    // THÊM VÀO AdminCategoryFragment.java và AdminDishFragment.java
    private void loadData() {
        try {
            categories.clear();
            List<Category> loadedCategories = categoryDAO.getAllCategories();
            if (loadedCategories != null) {
                categories.addAll(loadedCategories);
            }
            if (adapter != null) adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("AdminCategoryFragment", "Error loading categories", e);
        }
    }

    // Refresh method if needed
    public void refresh() {
        loadData();
    }
}
