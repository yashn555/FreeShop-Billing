package com.freeshop.billing.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.freeshop.billing.data.db.entity.Bill;

import java.util.List;

@Dao
public interface BillDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bill);

    @Query("SELECT * FROM bills WHERE shopId = :shopId ORDER BY createdAt DESC")
    LiveData<List<Bill>> observeAll(long shopId);

    @Query("SELECT * FROM bills WHERE shopId = :shopId AND (billNo LIKE '%' || :q || '%' OR customerName LIKE '%' || :q || '%') ORDER BY createdAt DESC")
    LiveData<List<Bill>> search(long shopId, String q);

    @Query("SELECT COUNT(*) FROM bills WHERE shopId = :shopId AND billNo = :billNo")
    int countByBillNo(long shopId, String billNo);

    @Query("SELECT * FROM bills WHERE id = :id")
    Bill getById(long id);
}
