package com.freeshop.billing.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {

    private val KEY_SHOP_ID = longPreferencesKey("shopId")

    val shopIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_SHOP_ID] ?: -1L
        if (id < 1) null else id
    }

    suspend fun setShopId(id: Long) {
        context.dataStore.edit { it[KEY_SHOP_ID] = id }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(KEY_SHOP_ID) }
    }
}
