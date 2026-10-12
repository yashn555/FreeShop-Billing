package com.freeshop.billing.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [ForeignKey(
        entity = Shop::class,
        parentColumns = ["id"],
        childColumns  = ["shopId"],
        onDelete      = ForeignKey.CASCADE,
    )],
    indices = [Index("shopId")],
)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val billNo: String,
    val date: String,                  // DD/MM/YYYY
    val customerName: String,
    val customerPhone: String = "",
    val customerGstin: String = "",
    val customerAddress: String = "",
    val itemsJson: String,             // JSON array of BillItem
    val subtotal: Double,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val received: Double,
    val balance: Double,
    val paymentMode: String = "CASH",
    val notes: String = "",
    val pdfPath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val documentType: String = "INVOICE",
    val isConverted: Boolean = false,
)
