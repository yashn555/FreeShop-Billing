package com.freeshop.billing.ui.viewmodel;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.freeshop.billing.data.db.AppDatabase;
import com.freeshop.billing.data.db.entity.Shop;
import com.freeshop.billing.data.repository.ShopRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsViewModel extends ViewModel {

    private final ShopRepository shopRepo;
    private final long shopId;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final LiveData<Shop> shop;
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);

    public LiveData<Shop> getShop() { return shop; }
    public LiveData<Boolean> getSaving() { return saving; }

    public SettingsViewModel(ShopRepository shopRepo, long shopId, Context context) {
        this.shopRepo = shopRepo;
        this.shopId = shopId;
        this.context = context.getApplicationContext();
        this.shop = shopRepo.observe(shopId);
    }

    public void save(Shop updated, Runnable onSuccess, Consumer<String> onError) {
        if (updated == null || updated.getName() == null || updated.getName().trim().isEmpty()) {
            if (onError != null) onError.accept("Shop name is required");
            return;
        }

        saving.setValue(true);
        executor.execute(() -> {
            shopRepo.update(updated);
            saving.postValue(false);
            if (onSuccess != null) {
                new Handler(Looper.getMainLooper()).post(onSuccess);
            }
        });
    }

    public void pickLogo(Uri uri, Consumer<String> onError) {
        executor.execute(() -> {
            try {
                InputStream input = context.getContentResolver().openInputStream(uri);
                if (input == null) {
                    postError(onError, "Cannot open image");
                    return;
                }

                long size = 0L;
                AssetFileDescriptor afd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                if (afd != null) {
                    size = afd.getLength();
                    afd.close();
                }

                if (size > 2 * 1024 * 1024) {
                    input.close();
                    postError(onError, "Logo must be under 2 MB");
                    return;
                }

                String mime = context.getContentResolver().getType(uri);
                if (mime == null || !mime.startsWith("image/")) {
                    input.close();
                    postError(onError, "Only PNG/JPG/WEBP allowed");
                    return;
                }

                String ext = "jpg";
                if (mime.contains("png")) ext = "png";
                else if (mime.contains("webp")) ext = "webp";

                File dir = new File(context.getFilesDir(), "logos");
                if (!dir.exists()) dir.mkdirs();

                File dest = new File(dir, "logo_" + shopId + "." + ext);
                try (FileOutputStream out = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = input.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                input.close();

                Shop current = shop.getValue();
                if (current != null) {
                    current.setLogoPath(dest.getAbsolutePath());
                    shopRepo.update(current);
                }

            } catch (Exception e) {
                postError(onError, "Failed to save logo: " + e.getMessage());
            }
        });
    }

    private void postError(Consumer<String> onError, String msg) {
        if (onError != null) {
            new Handler(Looper.getMainLooper()).post(() -> onError.accept(msg));
        }
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
            return (T) new SettingsViewModel(new ShopRepository(db.shopDao()), shopId, context);
        }
    }
}
