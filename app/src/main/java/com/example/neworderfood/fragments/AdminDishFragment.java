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

import com.example.neworderfood.adapters.AdminDishAdapter;
import com.example.neworderfood.room.AppDatabase;
import com.example.neworderfood.room.daos.DishDao;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.room.entities.DishEntity;
import com.example.neworderfood.activities.AdminManagementActivity;  // SỬA: Import đúng package

import java.util.ArrayList;
import java.util.List;

public class AdminDishFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminDishAdapter adapter;
    private List<Dish> dishes = new ArrayList<>();
    private AppDatabase db;
    private DishDao dishDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dish, container, false);

        db = AppDatabase.getInstance(getContext());
        dishDAO = db.dishDao();

        recyclerView = view.findViewById(R.id.rv_dishes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();

        adapter = new AdminDishAdapter(dishes, dish -> {
            if (getActivity() instanceof AdminManagementActivity) {  // SỬA: Import và package đúng
                ((AdminManagementActivity) getActivity()).showEditDishDialog(dish);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // THÊM VÀO AdminCategoryFragment.java và AdminDishFragment.java
    private void loadData() {
        try {
            dishes.clear();
            // USE SYNC: Load all dishes sync
            List<DishEntity> entities = dishDAO.getAllDishesSync();
            if (entities != null) {
                dishes.addAll(Dish.fromEntities(entities));
            }
            if (adapter != null) adapter.updateDishes(dishes);  // Use update method in adapter
        } catch (Exception e) {
            Log.e("AdminDishFragment", "Error loading dishes", e);
        }
    }

    public void refresh() {
        loadData();
    }
}