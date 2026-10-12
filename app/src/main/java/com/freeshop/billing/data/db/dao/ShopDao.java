package com.freeshop.billing.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.freeshop.billing.data.db.entity.Shop;

@Dao
public interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Shop shop);

    @Update
    void update(Shop shop);

    @Query("SELECT * FROM shops WHERE id = :id")
    Shop getById(long id);

    @Query("SELECT * FROM shops WHERE email = :email LIMIT 1")
    Shop getByEmail(String email);

    @Query("SELECT * FROM shops WHERE id = :id")
    LiveData<Shop> observeById(long id);
}
