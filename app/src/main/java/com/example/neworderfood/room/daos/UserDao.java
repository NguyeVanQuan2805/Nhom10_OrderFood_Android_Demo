package com.example.neworderfood.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.neworderfood.room.entities.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsersSync();
    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    UserEntity getUserByUsernameAndPassword(String username, String password);

    // FIXED: Use EXISTS query to return boolean directly, avoiding COUNT type clash
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username LIMIT 1)")
    boolean isUsernameExists(String username);

    @Insert
    long addUser(UserEntity user);

    @Update
    int updateUser(UserEntity user);

    @Query("DELETE FROM users WHERE id = :id")
    int deleteUser(int id);
}