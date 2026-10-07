package com.freeshop.billing.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freeshop.billing.data.db.AppDatabase
import com.freeshop.billing.data.db.entity.Product
import com.freeshop.billing.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ItemsViewModel(
    private val repo: ProductRepository,
    private val shopId: Long,
) : ViewModel() {

    val items: StateFlow<List<Product>> = repo.observeAll(shopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun add(name: String, unit: String, price: String, taxRate: String, hsn: String, onDone: () -> Unit) {
        if (name.isBlank()) { _error.value = "Item name is required"; return }
        val p = price.toDoubleOrNull() ?: run { _error.value = "Invalid price"; return }
        val t = taxRate.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            repo.insert(Product(shopId = shopId, name = name.trim(), unit = unit.ifBlank { "PCS" }, price = p, taxRate = t, hsn = hsn.trim()))
            onDone()
        }
    }

    fun delete(product: Product) {
        viewModelScope.launch { repo.delete(product) }
    }

    fun clearError() { _error.value = null }

    class Factory(private val context: Context, private val shopId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return ItemsViewModel(ProductRepository(db.productDao()), shopId) as T
        }
    }
}
