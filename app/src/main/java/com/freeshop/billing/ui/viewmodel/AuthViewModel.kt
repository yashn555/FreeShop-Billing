package com.freeshop.billing.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freeshop.billing.data.SessionStore
import com.freeshop.billing.data.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val shopRepo: ShopRepository,
    private val session: SessionStore,
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val sessionShopId: Flow<Long?> = session.shopIdFlow

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun register(
        name: String, email: String, password: String, confirm: String,
        onSuccess: () -> Unit,
    ) {
        if (name.isBlank())   { _error.value = "Shop name is required"; return }
        if (email.isBlank())  { _error.value = "Email is required"; return }
        if (!emailRegex.matches(email.trim())) { _error.value = "Invalid email address"; return }
        if (password.length < 6) { _error.value = "Password must be at least 6 characters"; return }
        if (password != confirm)  { _error.value = "Passwords do not match"; return }

        _loading.value = true
        viewModelScope.launch {
            try {
                val id = shopRepo.register(name.trim(), email.trim().lowercase(), password)
                session.setShopId(id)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Email already registered"
            } finally {
                _loading.value = false
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (email.isBlank() || password.isBlank()) { _error.value = "Email and password required"; return }
        _loading.value = true
        viewModelScope.launch {
            val shop = shopRepo.login(email.trim().lowercase(), password)
            _loading.value = false
            if (shop != null) {
                session.setShopId(shop.id)
                _error.value = null
                onSuccess()
            } else {
                _error.value = "Invalid email or password"
                onFailure()
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            session.clear()
            onDone()
        }
    }

    fun clearError() { _error.value = null }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db   = com.freeshop.billing.data.db.AppDatabase.getInstance(context)
            val repo = ShopRepository(db.shopDao())
            val sess = SessionStore(context)
            return AuthViewModel(repo, sess) as T
        }
    }
}
