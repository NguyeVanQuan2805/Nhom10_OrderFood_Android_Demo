package com.example.neworderfood.adapters;  // SỬA package

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.neworderfood.activitis.AdminManagementActivity;  // SỬA import
import com.example.neworderfood.fragment.AdminCategoryFragment;
import com.example.neworderfood.fragment.AdminDishFragment;
import com.example.neworderfood.fragment.TableManagementFragment;  // THÊM import
//import com.example.tt7.fragment.UserManagementFragment;  // THÊM khi có
import com.example.neworderfood.fragment.UserManagementFragment;
import com.example.neworderfood.models.Category;
import com.example.neworderfood.models.Dish;
import com.example.neworderfood.models.Table;
import com.example.neworderfood.models.User;

import java.util.List;

public class AdminPagerAdapter extends FragmentStateAdapter {
    private List<Category> categories;
    private List<Dish> dishes;
    private List<Table> tables;
    private List<User> users;  // THÊM

    // SỬA: Constructor đầy đủ, init fields
    public AdminPagerAdapter(AdminManagementActivity fa, List<Category> categories, List<Dish> dishes, List<Table> tables, List<User> users) {
        super(fa);
        this.categories = categories != null ? categories : List.of();
        this.dishes = dishes != null ? dishes : List.of();
        this.tables = tables != null ? tables : List.of();
        this.users = users != null ? users : List.of();
    }

    // BỎ constructor cũ (chỉ giữ 1)

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putInt("position", position);  // Truyền data nếu cần
        switch (position) {
            case 0:
                AdminCategoryFragment fragment = new AdminCategoryFragment();
                fragment.setArguments(args);
                return fragment;
            case 1:
                AdminDishFragment fragment1 = new AdminDishFragment();
                fragment1.setArguments(args);
                return fragment1;
            case 2:
                TableManagementFragment tableFragment = new TableManagementFragment();  // SỬA
                tableFragment.setArguments(args);
                return tableFragment;
            case 3:
                UserManagementFragment userFragment = new UserManagementFragment();
                userFragment.setArguments(args);
                return userFragment;
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;  // SỬA: 4 tabs
    }
}