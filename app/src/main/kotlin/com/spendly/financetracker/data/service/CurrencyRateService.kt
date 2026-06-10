package com.spendly.financetracker.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.spendly.financetracker.data.local.dao.ExchangeRateDao
import com.spendly.financetracker.data.local.entity.ExchangeRateEntity
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
class CurrencyRateService @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao
) {
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
            val now = System.currentTimeMillis()
            val cachedRow = exchangeRateDao.getRate(from, to)
            cachedRow?.takeIf { it.expiresAtMillis > now }?.let {
                val result = RateResult(rate = it.rate, source = "CACHE", fetchedAtMillis = it.fetchedAtMillis)
                cache[key] = result
                return@runCatching result
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
                    exchangeRateDao.upsert(
                        ExchangeRateEntity(
                            id = key,
                            fromCurrency = from,
                            toCurrency = to,
                            rate = rate,
                            source = "API",
                            fetchedAtMillis = it.fetchedAtMillis,
                            expiresAtMillis = it.fetchedAtMillis + CACHE_TTL_MILLIS
                        )
                    )
                }
            } catch (error: Exception) {
                cachedRow?.let {
                    return@runCatching RateResult(rate = it.rate, source = "STALE_CACHE", fetchedAtMillis = it.fetchedAtMillis)
                }
                throw error
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        private const val CACHE_TTL_MILLIS = 15 * 60 * 1000L
    }
}
