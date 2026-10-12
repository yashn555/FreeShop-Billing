package com.freeshop.billing.data.repository;

import androidx.lifecycle.LiveData;

import com.freeshop.billing.data.db.dao.ProductDao;
import com.freeshop.billing.data.db.entity.Product;

import java.util.List;

public class ProductRepository {

    private final ProductDao dao;

    public ProductRepository(ProductDao dao) {
        this.dao = dao;
    }

    public LiveData<List<Product>> observeAll(long shopId) {
        return dao.observeAll(shopId);
    }

    public List<Product> getAll(long shopId) {
        return dao.getAll(shopId);
    }

    public List<Product> search(long shopId, String q) {
        return dao.search(shopId, q);
    }

    public long insert(Product product) {
        return dao.insert(product);
    }

    public void delete(Product product) {
        dao.delete(product);
    }
}
