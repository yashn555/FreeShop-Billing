package com.freeshop.billing.data.repository;

import androidx.lifecycle.LiveData;

import com.freeshop.billing.data.db.dao.CustomerDao;
import com.freeshop.billing.data.db.entity.Customer;

import java.util.List;

public class CustomerRepository {

    private final CustomerDao dao;

    public CustomerRepository(CustomerDao dao) {
        this.dao = dao;
    }

    public LiveData<List<Customer>> observeAll(long shopId) {
        return dao.observeAll(shopId);
    }

    public List<Customer> getAll(long shopId) {
        return dao.getAll(shopId);
    }

    public List<Customer> search(long shopId, String q) {
        return dao.search(shopId, q);
    }

    public long insert(Customer customer) {
        return dao.insert(customer);
    }

    public void delete(Customer customer) {
        dao.delete(customer);
    }
}
