package com.freeshop.billing.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freeshop.billing.data.db.AppDatabase
import com.freeshop.billing.data.db.entity.Shop
import com.freeshop.billing.data.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val shopRepo: ShopRepository,
    private val shopId: Long,
    private val context: Context,
) : ViewModel() {

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    init {
        viewModelScope.launch {
            shopRepo.observe(shopId).collect { _shop.value = it }
        }
    }

    fun save(updated: Shop, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (updated.name.isBlank()) { onError("Shop name is required"); return }
        _saving.value = true
        viewModelScope.launch {
            shopRepo.update(updated)
            _saving.value = false
            onSuccess()
        }
    }

    fun pickLogo(uri: Uri, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open image")
                val size = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                if (size > 2 * 1024 * 1024) {
                    onError("Logo must be under 2 MB")
                    return@launch
                }
                val mime = context.contentResolver.getType(uri) ?: ""
                if (!mime.startsWith("image/")) {
                    onError("Only PNG/JPG/WEBP allowed")
                    return@launch
                }
                val ext  = when { mime.contains("png") -> "png"; mime.contains("webp") -> "webp"; else -> "jpg" }
                val dir  = File(context.filesDir, "logos").also { it.mkdirs() }
                val dest = File(dir, "logo_${shopId}.$ext")
                dest.outputStream().use { out -> input.copyTo(out) }
                input.close()
                val current = _shop.value ?: return@launch
                shopRepo.update(current.copy(logoPath = dest.absolutePath))
            } catch (e: Exception) {
                onError("Failed to save logo: ${e.message}")
            }
        }
    }

    class Factory(private val context: Context, private val shopId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return SettingsViewModel(ShopRepository(db.shopDao()), shopId, context) as T
        }
    }
}
