package com.freeshop.billing.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    foreignKeys = [ForeignKey(
        entity = Shop::class,
        parentColumns = ["id"],
        childColumns  = ["shopId"],
        onDelete      = ForeignKey.CASCADE,
    )],
    indices = [Index("shopId")],
)
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val gstin: String = "",
    val address: String = "",
)
