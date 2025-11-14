package com.example.neworderfood.room.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.neworderfood.room.entities.TableEntity;

import java.util.List;

@Dao
public interface TableDao {
    @Query("SELECT * FROM tables ORDER BY number ASC")
    LiveData<List<TableEntity>> getAllTablesLiveData();


    @Query("SELECT * FROM tables ORDER BY number ASC")
    List<TableEntity> getAllTables();

    @Query("SELECT * FROM tables WHERE status = :status ORDER BY number ASC")
    List<TableEntity> getTablesByStatus(String status);

    @Query("SELECT * FROM tables WHERE id = :id")
    TableEntity getTableById(int id);

    @Query("SELECT * FROM tables WHERE number = :number")
    TableEntity getTableByNumber(int number);

    @Insert
    long addTable(TableEntity table);

    @Update
    int updateTable(TableEntity table);

    @Query("DELETE FROM tables WHERE id = :id")
    int deleteTable(int id);

    // Custom update status by number (no direct ID)
    @Query("UPDATE tables SET status = :status WHERE number = :number")
    int updateTableStatusByNumber(int number, String status);

    @Query("UPDATE tables SET status = :status WHERE id = :id")
    int updateTableStatus(int id, String status);
}