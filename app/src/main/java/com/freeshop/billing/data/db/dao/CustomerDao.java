package com.freeshop.billing.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.freeshop.billing.data.db.entity.Customer;

import java.util.List;

@Dao
public interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Customer customer);

    @Delete
    void delete(Customer customer);

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    LiveData<List<Customer>> observeAll(long shopId);

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    List<Customer> getAll(long shopId);

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND name LIKE '%' || :q || '%' ORDER BY name ASC")
    List<Customer> search(long shopId, String q);
}
