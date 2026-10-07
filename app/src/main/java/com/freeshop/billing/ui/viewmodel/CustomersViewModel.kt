package com.freeshop.billing.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freeshop.billing.data.db.AppDatabase
import com.freeshop.billing.data.db.entity.Customer
import com.freeshop.billing.data.repository.CustomerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomersViewModel(
    private val repo: CustomerRepository,
    private val shopId: Long,
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = repo.observeAll(shopId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun add(name: String, phone: String, email: String, gstin: String, address: String, onDone: () -> Unit) {
        if (name.isBlank()) { _error.value = "Customer name is required"; return }
        viewModelScope.launch {
            repo.insert(Customer(shopId = shopId, name = name.trim(), phone = phone.trim(), email = email.trim(), gstin = gstin.trim(), address = address.trim()))
            onDone()
        }
    }

    fun delete(customer: Customer) {
        viewModelScope.launch { repo.delete(customer) }
    }

    fun clearError() { _error.value = null }

    class Factory(private val context: Context, private val shopId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return CustomersViewModel(CustomerRepository(db.customerDao()), shopId) as T
        }
    }
}
