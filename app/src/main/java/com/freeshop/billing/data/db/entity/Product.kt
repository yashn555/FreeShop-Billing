package com.freeshop.billing.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = Shop::class,
        parentColumns = ["id"],
        childColumns  = ["shopId"],
        onDelete      = ForeignKey.CASCADE,
    )],
    indices = [Index("shopId")],
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val name: String,
    val unit: String = "PCS",
    val price: Double = 0.0,
    val taxRate: Double = 0.0,
    val hsn: String = "",
)
