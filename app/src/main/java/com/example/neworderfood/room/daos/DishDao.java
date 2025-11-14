package com.example.neworderfood.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.neworderfood.room.entities.DishEntity;

import java.util.List;

@Dao
public interface DishDao {
    @Query("SELECT * FROM dishes")
    List<DishEntity> getAllDishesSync();  // NEW: Sync

    @Query("SELECT * FROM dishes WHERE categoryId = :categoryId ORDER BY name")
    List<DishEntity> getDishesByCategorySync(int categoryId);
    @Query("SELECT * FROM dishes WHERE categoryId = :categoryId ORDER BY name")
    LiveData<List<DishEntity>> getDishesByCategory(int categoryId);

    @Query("SELECT * FROM dishes")
    LiveData<List<DishEntity>> getAllDishes();

    @Query("SELECT * FROM dishes WHERE id = :id")
    DishEntity getDishById(int id);

    @Query("SELECT COUNT(*) FROM dishes WHERE categoryId = :categoryId")
    int getDishCountByCategory(int categoryId);


    @Insert
    long addDish(DishEntity dish);

    @Update
    int updateDish(DishEntity dish);

    @Query("DELETE FROM dishes WHERE id = :id")
    int deleteDish(int id);
}