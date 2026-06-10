package com.spendly.financetracker.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRateService @Inject constructor() {
    private val cache = mutableMapOf<String, RateResult>()

    suspend fun getRate(coinSymbol: String, defaultCurrency: String): Result<RateResult> = withContext(Dispatchers.IO) {
        runCatching {
            if (coinSymbol == "Other") {
                throw IllegalArgumentException("Manual rate required")
            }
            val coinId = coinIdFor(coinSymbol)
            val currency = defaultCurrency.lowercase()
            val key = "$coinId-$currency"
            cache[key]?.takeIf { System.currentTimeMillis() - it.fetchedAtMillis < CACHE_TTL_MILLIS }?.let {
                return@runCatching it.copy(source = "CACHE")
            }

            val url = URL("https://api.coingecko.com/api/v3/simple/price?ids=$coinId&vs_currencies=$currency")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "GET"
            }
            try {
                if (connection.responseCode == 429) {
                    cache[key]?.let { return@runCatching it.copy(source = "CACHE") }
                    throw IllegalStateException("Rate limited")
                }
                if (connection.responseCode !in 200..299) {
                    throw IllegalStateException("Crypto rate unavailable (${connection.responseCode})")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val coin = JSONObject(body).optJSONObject(coinId)
                    ?: throw IllegalStateException("Crypto rate unavailable")
                val rate = coin.optDouble(currency, 0.0)
                if (rate <= 0.0) throw IllegalStateException("Crypto rate unavailable")
                RateResult(rate = rate, source = "API", fetchedAtMillis = System.currentTimeMillis()).also {
                    cache[key] = it
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun coinIdFor(symbol: String): String = when (symbol.uppercase()) {
        "BTC" -> "bitcoin"
        "ETH" -> "ethereum"
        "USDT" -> "tether"
        "BNB" -> "binancecoin"
        "SOL" -> "solana"
        "XRP" -> "ripple"
        "DOGE" -> "dogecoin"
        else -> symbol.lowercase()
    }

    companion object {
        private const val CACHE_TTL_MILLIS = 15 * 60 * 1000L
    }
}
