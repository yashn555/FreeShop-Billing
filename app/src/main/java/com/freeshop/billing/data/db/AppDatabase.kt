package com.freeshop.billing.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freeshop.billing.data.db.dao.*
import com.freeshop.billing.data.db.entity.*

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bills ADD COLUMN documentType TEXT NOT NULL DEFAULT 'INVOICE'")
        db.execSQL("ALTER TABLE bills ADD COLUMN isConverted INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [Shop::class, Customer::class, Product::class, Bill::class],
    version  = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun billDao(): BillDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freeshop.db",
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
    }
}
