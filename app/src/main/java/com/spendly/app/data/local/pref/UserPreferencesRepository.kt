package com.spendly.app.data.local.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spendly.app.data.model.enums.Currency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    private val USD_TO_LKR_RATE = doublePreferencesKey("usd_to_lkr_rate")
    private val SYNC_OVER_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")

    val defaultCurrency: Flow<Currency> = context.dataStore.data.map { prefs ->
        val name = prefs[DEFAULT_CURRENCY] ?: Currency.LKR.name
        Currency.valueOf(name)
    }

    val usdToLkrRate: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[USD_TO_LKR_RATE] ?: 320.5
    }

    val syncOverWifiOnly: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SYNC_OVER_WIFI_ONLY] ?: false
    }

    suspend fun setDefaultCurrency(currency: Currency) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_CURRENCY] = currency.name
        }
    }

    suspend fun setUsdToLkrRate(rate: Double) {
        context.dataStore.edit { prefs ->
            prefs[USD_TO_LKR_RATE] = rate
        }
    }

    suspend fun setSyncOverWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SYNC_OVER_WIFI_ONLY] = wifiOnly
        }
    }
}
