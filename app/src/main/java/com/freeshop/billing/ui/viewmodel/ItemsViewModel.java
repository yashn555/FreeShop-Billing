package com.freeshop.billing.ui.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.freeshop.billing.data.db.AppDatabase;
import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.data.repository.ProductRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemsViewModel extends ViewModel {

    private final ProductRepository repo;
    private final long shopId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LiveData<List<Product>> items;
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<List<Product>> getItems() { return items; }
    public LiveData<String> getError() { return error; }

    public ItemsViewModel(ProductRepository repo, long shopId) {
        this.repo = repo;
        this.shopId = shopId;
        this.items = repo.observeAll(shopId);
    }

    public void add(String name, String unit, String price, String taxRate, String hsn, Runnable onDone) {
        if (name == null || name.trim().isEmpty()) {
            error.setValue("Item name is required");
            return;
        }

        double p;
        try {
            p = Double.parseDouble(price.trim());
        } catch (Exception e) {
            error.setValue("Invalid price");
            return;
        }

        double t = 0.0;
        if (taxRate != null && !taxRate.trim().isEmpty()) {
            try {
                t = Double.parseDouble(taxRate.trim());
            } catch (Exception ignored) {}
        }

        final double finalPrice = p;
        final double finalTaxRate = t;

        executor.execute(() -> {
            Product product = new Product(
                    shopId,
                    name.trim(),
                    (unit != null && !unit.trim().isEmpty()) ? unit.trim() : "PCS",
                    finalPrice,
                    finalTaxRate,
                    hsn != null ? hsn.trim() : ""
            );
            repo.insert(product);
            if (onDone != null) {
                new Handler(Looper.getMainLooper()).post(onDone);
            }
        });
    }

    public void delete(Product product) {
        executor.execute(() -> repo.delete(product));
    }

    public void clearError() {
        error.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Context context;
        private final long shopId;

        public Factory(Context context, long shopId) {
            this.context = context.getApplicationContext();
            this.shopId = shopId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            AppDatabase db = AppDatabase.getInstance(context);
            return (T) new ItemsViewModel(new ProductRepository(db.productDao()), shopId);
        }
    }
}
