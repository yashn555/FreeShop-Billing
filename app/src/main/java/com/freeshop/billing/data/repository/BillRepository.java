package com.freeshop.billing.data.repository;

import androidx.lifecycle.LiveData;

import com.freeshop.billing.data.db.dao.BillDao;
import com.freeshop.billing.data.db.entity.Bill;

import java.util.List;

public class BillRepository {

    private final BillDao dao;

    public BillRepository(BillDao dao) {
        this.dao = dao;
    }

    public LiveData<List<Bill>> observeAll(long shopId) {
        return dao.observeAll(shopId);
    }

    public LiveData<List<Bill>> search(long shopId, String q) {
        return dao.search(shopId, q);
    }

    public long insert(Bill bill) {
        return dao.insert(bill);
    }

    public void update(Bill bill) {
        dao.update(bill);
    }

    public void delete(Bill bill) {
        dao.delete(bill);
    }

    public boolean isBillNoUnique(long shopId, String billNo) {
        return dao.countByBillNo(shopId, billNo) == 0;
    }

    public Bill getById(long id) {
        return dao.getById(id);
    }
}
