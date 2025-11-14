package com.example.neworderfood.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.neworderfood.room.entities.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    List<CategoryEntity> getAllCategoriesSync();

    @Query("SELECT * FROM categories ORDER BY name")
    LiveData<List<CategoryEntity>> getAllCategories();  // LiveData for auto-update

    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity getCategoryById(int id);

    @Insert
    long addCategory(CategoryEntity category);

    @Update
    int updateCategory(CategoryEntity category);

    @Delete
    int deleteCategory(CategoryEntity category);  // Or use @Query("DELETE FROM categories WHERE id = :id")
    @Query("DELETE FROM categories WHERE id = :id")
    int deleteCategoryById(int id);

}