package com.freeshop.billing.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.freeshop.billing.data.db.dao.BillDao;
import com.freeshop.billing.data.db.dao.CustomerDao;
import com.freeshop.billing.data.db.dao.ProductDao;
import com.freeshop.billing.data.db.dao.ShopDao;
import com.freeshop.billing.data.db.entity.Bill;
import com.freeshop.billing.data.db.entity.Customer;
import com.freeshop.billing.data.db.entity.Product;
import com.freeshop.billing.data.db.entity.Shop;

@Database(
    entities = {Shop.class, Customer.class, Product.class, Bill.class},
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ShopDao shopDao();
    public abstract CustomerDao customerDao();
    public abstract ProductDao productDao();
    public abstract BillDao billDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE bills ADD COLUMN documentType TEXT NOT NULL DEFAULT 'INVOICE'");
            db.execSQL("ALTER TABLE bills ADD COLUMN isConverted INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "freeshop.db"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
