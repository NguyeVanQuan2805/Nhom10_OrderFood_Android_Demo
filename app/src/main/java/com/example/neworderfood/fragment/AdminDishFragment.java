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

import com.example.neworderfood.adapters.AdminDishAdapter;
import com.example.neworderfood.database.DatabaseHelper;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.models.Dish;

import java.util.ArrayList;
import java.util.List;

public class AdminDishFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminDishAdapter adapter;
    private List<Dish> dishes = new ArrayList<>();
    private DishDAO dishDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dish, container, false);
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        dishDAO = dbHelper.getDishDAO();
        recyclerView = view.findViewById(R.id.rv_dishes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadData();
        adapter = new AdminDishAdapter(dishes, dish -> {
            if (getActivity() instanceof com.example.neworderfood.activitis.AdminManagementActivity) {
                ((com.example.neworderfood.activitis.AdminManagementActivity) getActivity()).showEditDishDialog(dish);
            }
        });
        recyclerView.setAdapter(adapter);
        return view;
    }

    // THÊM VÀO AdminCategoryFragment.java và AdminDishFragment.java
    private void loadData() {
        try {
            dishes.clear();
            List<Dish> loadedDishes = dishDAO.getAllDishes();
            if (loadedDishes != null) {
                dishes.addAll(loadedDishes);
            }
            if (adapter != null) adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("AdminDishFragment", "Error loading dishes", e);
        }
    }

    public void refresh() {
        loadData();
    }
}