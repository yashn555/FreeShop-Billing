package com.freeshop.billing.domain

object Validators {
    fun validateCustomerName(name: String): String =
        name.trim().ifBlank { "Walk-in Customer" }

    fun validateBillNo(billNo: String): String? =
        if (billNo.isBlank()) "Bill number is required" else null

    fun validateItems(items: List<BillItem>): String? {
        if (items.isEmpty()) return "Add at least one item"
        items.forEachIndexed { i, item ->
            if (item.name.isBlank()) return "Item ${i + 1}: name is required"
            if (item.qty <= 0)       return "Item ${i + 1}: quantity must be > 0"
            if (item.rate < 0)       return "Item ${i + 1}: rate must be >= 0"
            if (item.taxRate < 0)    return "Item ${i + 1}: tax rate must be >= 0"
        }
        return null
    }

    fun validateDiscount(discount: Double, subtotal: Double): String? {
        if (discount < 0) return "Discount must be >= 0"
        if (discount > subtotal) return "Discount cannot exceed subtotal"
        return null
    }

    fun validateReceived(received: Double): String? {
        if (received < 0) return "Received amount must be >= 0"
        return null
    }
}

data class BillItem(
    val name: String,
    val hsn: String = "",
    val qty: Double,
    val rate: Double,
    val taxRate: Double = 0.0,
    val unit: String = "PCS",
) {
    val lineTotal: Double get() = qty * rate
    val taxAmount: Double get() = lineTotal * taxRate / 100.0
}
