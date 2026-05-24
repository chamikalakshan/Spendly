package com.spendly.financetracker.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class RateResult(
    val rate: Double,
    val source: String,
    val fetchedAtMillis: Long
)

@Singleton
class CurrencyRateService @Inject constructor() {
    private val cache = mutableMapOf<String, RateResult>()

    suspend fun getRate(fromCurrency: String, toCurrency: String): Result<RateResult> = withContext(Dispatchers.IO) {
        runCatching {
            val from = fromCurrency.uppercase()
            val to = toCurrency.uppercase()
            if (from == to) {
                return@runCatching RateResult(rate = 1.0, source = "MANUAL", fetchedAtMillis = System.currentTimeMillis())
            }

            val key = "$from-$to"
            cache[key]?.takeIf { System.currentTimeMillis() - it.fetchedAtMillis < CACHE_TTL_MILLIS }?.let {
                return@runCatching it.copy(source = "CACHE")
            }

            val url = URL("https://api.exchangerate.host/latest?base=$from&symbols=$to")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "GET"
            }
            try {
                if (connection.responseCode !in 200..299) {
                    throw IllegalStateException("Rate unavailable (${connection.responseCode})")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val rates = JSONObject(body).optJSONObject("rates")
                    ?: throw IllegalStateException("Rate unavailable")
                val rate = rates.optDouble(to, 0.0)
                if (rate <= 0.0) throw IllegalStateException("Rate unavailable")
                RateResult(rate = rate, source = "API", fetchedAtMillis = System.currentTimeMillis()).also {
                    cache[key] = it
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        private const val CACHE_TTL_MILLIS = 15 * 60 * 1000L
    }
}
