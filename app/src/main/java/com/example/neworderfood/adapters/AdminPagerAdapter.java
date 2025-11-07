package com.example.neworderfood.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.neworderfood.activitis.AdminManagementActivity;
import com.example.neworderfood.dao.CategoryDAO;
import com.example.neworderfood.dao.DishDAO;
import com.example.neworderfood.fragment.AdminCategoryFragment;
import com.example.neworderfood.fragment.AdminDishFragment;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;

import java.util.List;


public class AdminPagerAdapter extends FragmentStateAdapter {

    public AdminPagerAdapter(AdminManagementActivity fa, List<Category> categories, List<Dish> dishes, CategoryDAO categoryDAO, DishDAO dishDAO) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new AdminCategoryFragment();
        } else {
            return new AdminDishFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}