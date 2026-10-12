package com.freeshop.billing.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstin: String = "",
    val logoPath: String = "",
    val invoicePrefix: String = "INV",
    val nextInvoiceNo: Int = 1,
    val taxEnabled: Boolean = false,
    val taxLabel: String = "GST",
    val taxRate: Double = 18.0,
    val bankName: String = "",
    val accountNo: String = "",
    val ifsc: String = "",
    val upiId: String = "",
    val terms: String = "",
)
