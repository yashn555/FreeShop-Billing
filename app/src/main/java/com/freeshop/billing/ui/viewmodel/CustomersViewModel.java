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
import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.data.repository.CustomerRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomersViewModel extends ViewModel {

    private final CustomerRepository repo;
    private final long shopId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LiveData<List<Customer>> customers;
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<List<Customer>> getCustomers() { return customers; }
    public LiveData<String> getError() { return error; }

    public CustomersViewModel(CustomerRepository repo, long shopId) {
        this.repo = repo;
        this.shopId = shopId;
        this.customers = repo.observeAll(shopId);
    }

    public void add(String name, String phone, String email, String gstin, String address, Runnable onDone) {
        if (name == null || name.trim().isEmpty()) {
            error.setValue("Customer name is required");
            return;
        }

        executor.execute(() -> {
            Customer customer = new Customer(
                    shopId,
                    name.trim(),
                    phone != null ? phone.trim() : "",
                    email != null ? email.trim() : "",
                    gstin != null ? gstin.trim() : "",
                    address != null ? address.trim() : ""
            );
            repo.insert(customer);
            if (onDone != null) {
                new Handler(Looper.getMainLooper()).post(onDone);
            }
        });
    }

    public void delete(Customer customer) {
        executor.execute(() -> repo.delete(customer));
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
            return (T) new CustomersViewModel(new CustomerRepository(db.customerDao()), shopId);
        }
    }
}
