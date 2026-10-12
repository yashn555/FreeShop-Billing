package com.freeshop.billing.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.freeshop.billing.data.db.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    LiveData<List<Product>> observeAll(long shopId);

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    List<Product> getAll(long shopId);

    @Query("SELECT * FROM products WHERE shopId = :shopId AND name LIKE '%' || :q || '%' ORDER BY name ASC")
    List<Product> search(long shopId, String q);
}
