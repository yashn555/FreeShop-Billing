package com.freeshop.billing.domain

import com.freeshop.billing.data.db.entity.Bill

enum class PaymentStatus {
    PAID,
    PARTIAL,
    UNPAID
}

val Bill.paymentStatus: PaymentStatus
    get() = when {
        received >= grandTotal -> PaymentStatus.PAID
        received > 0.0 -> PaymentStatus.PARTIAL
        else -> PaymentStatus.UNPAID
    }
