package com.freeshop.billing.ui.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.freeshop.billing.data.SessionStore;
import com.freeshop.billing.data.db.AppDatabase;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.data.repository.ShopRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class AuthViewModel extends ViewModel {

    private final ShopRepository shopRepo;
    private final SessionStore session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public AuthViewModel(ShopRepository shopRepo, SessionStore session) {
        this.shopRepo = shopRepo;
        this.session = session;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public Long getSessionShopId() {
        return session.getShopId();
    }

    public void register(String name, String email, String password, String confirm, Runnable onSuccess) {
        if (name == null || name.trim().isEmpty()) {
            error.setValue("Shop name is required");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            error.setValue("Email is required");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            error.setValue("Invalid email address");
            return;
        }
        if (password == null || password.length() < 6) {
            error.setValue("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            error.setValue("Passwords do not match");
            return;
        }

        loading.setValue(true);
        executor.execute(() -> {
            try {
                long id = shopRepo.register(name.trim(), email.trim().toLowerCase(), password);
                session.setShopId(id);
                error.postValue(null);
                if (onSuccess != null) {
                    new Handler(Looper.getMainLooper()).post(onSuccess);
                }
            } catch (Exception e) {
                error.postValue("Email already registered");
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void login(String email, String password, Runnable onSuccess, Runnable onFailure) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            error.setValue("Email and password required");
            return;
        }

        loading.setValue(true);
        executor.execute(() -> {
            Shop shop = shopRepo.login(email.trim().toLowerCase(), password);
            loading.postValue(false);
            if (shop != null) {
                session.setShopId(shop.getId());
                error.postValue(null);
                if (onSuccess != null) {
                    new Handler(Looper.getMainLooper()).post(onSuccess);
                }
            } else {
                error.postValue("Invalid email or password");
                if (onFailure != null) {
                    new Handler(Looper.getMainLooper()).post(onFailure);
                }
            }
        });
    }

    public void logout(Runnable onDone) {
        executor.execute(() -> {
            session.clear();
            if (onDone != null) {
                new Handler(Looper.getMainLooper()).post(onDone);
            }
        });
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

        public Factory(Context context) {
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            AppDatabase db = AppDatabase.getInstance(context);
            ShopRepository repo = new ShopRepository(db.shopDao());
            SessionStore sess = new SessionStore(context);
            return (T) new AuthViewModel(repo, sess);
        }
    }
}
