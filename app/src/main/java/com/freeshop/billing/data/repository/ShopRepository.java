package com.freeshop.billing.data.repository;

import androidx.lifecycle.LiveData;

import com.freeshop.billing.data.PasswordHasher;
import com.freeshop.billing.data.db.dao.ShopDao;
import com.freeshop.billing.data.db.entity.Shop;

public class ShopRepository {

    private final ShopDao dao;

    public ShopRepository(ShopDao dao) {
        this.dao = dao;
    }

    public long register(String name, String email, String password) {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(password, salt);
        Shop shop = new Shop(name, email, hash, salt);
        return dao.insert(shop);
    }

    public Shop login(String email, String password) {
        Shop shop = dao.getByEmail(email);
        if (shop == null) return null;
        if (PasswordHasher.verify(password, shop.getPasswordSalt(), shop.getPasswordHash())) {
            return shop;
        }
        return null;
    }

    public Shop getById(long id) {
        return dao.getById(id);
    }

    public void update(Shop shop) {
        dao.update(shop);
    }

    public LiveData<Shop> observe(long id) {
        return dao.observeById(id);
    }
}
