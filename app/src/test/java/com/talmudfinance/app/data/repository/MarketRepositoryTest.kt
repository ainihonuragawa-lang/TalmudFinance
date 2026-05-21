package com.talmudfinance.app.data.repository

import com.talmudfinance.app.data.model.MarketCategory
import com.talmudfinance.app.data.model.YahooChart
import com.talmudfinance.app.data.model.YahooChartResponse
import com.talmudfinance.app.data.model.YahooChartResult
import com.talmudfinance.app.data.model.YahooMeta
import com.talmudfinance.app.data.remote.YahooFinanceApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * MarketRepository のユニットテスト。
 * YahooFinanceApi をフェイクに差し替えて、ネットワーク非依存で検証する。
 */
class MarketRepositoryTest {

    /** 固定レスポンスを返すフェイクAPI */
    private class FakeApi(
        private val responses: Map<String, YahooChartResponse> = emptyMap(),
        private val errors: Set<String> = emptySet()
    ) : YahooFinanceApi {
        override suspend fun getQuote(
            symbol: String,
            interval: String,
            range: String
        ): YahooChartResponse {
            if (symbol in errors) throw IOException("Simulated network failure: $symbol")
            return responses[symbol] ?: YahooChartResponse(YahooChart(result = null))
        }
    }

    private fun validQuote(symbol: String, price: Double, prev: Double, currency: String) =
        YahooChartResponse(
            chart = YahooChart(
                result = listOf(
                    YahooChartResult(
                        meta = YahooMeta(
                            symbol = symbol,
                            currency = currency,
                            regularMarketPrice = price,
                            previousClose = prev
                        )
                    )
                )
            )
        )

    @Test
    fun `successful response is parsed into MarketQuote`() = runTest {
        val api = FakeApi(responses = mapOf(
            "^N225" to validQuote("^N225", 39000.0, 38500.0, "JPY")
        ))
        val repo = MarketRepository(api)
        val result = repo.fetchByCategory(MarketCategory.JP_STOCK)

        val n225 = result.firstOrNull { it.symbol == "^N225" }
        assertNotNull("Should fetch ^N225 successfully", n225)
        assertEquals(39000.0, n225!!.price, 0.001)
        assertEquals(38500.0, n225.previousClose, 0.001)
        assertEquals(500.0, n225.change, 0.001)
        assertTrue("Should be marked as up", n225.isUp)
        assertEquals("JPY", n225.currency)
    }

    @Test
    fun `failed symbols are silently dropped from results`() = runTest {
        val api = FakeApi(
            responses = mapOf(
                "^N225" to validQuote("^N225", 39000.0, 38500.0, "JPY")
            ),
            errors = setOf("^TPX")  // TPX は失敗する
        )
        val repo = MarketRepository(api)
        val result = repo.fetchByCategory(MarketCategory.JP_STOCK)

        // ^N225 は成功
        assertTrue("^N225 should be in result", result.any { it.symbol == "^N225" })
        // ^TPX は失敗で除外（クラッシュしない）
        assertFalse("^TPX should be excluded", result.any { it.symbol == "^TPX" })
    }

    @Test
    fun `empty response is treated as failure (excluded)`() = runTest {
        // 何も登録しないと FakeApi が result=null を返す
        val api = FakeApi()
        val repo = MarketRepository(api)
        val result = repo.fetchByCategory(MarketCategory.JP_STOCK)

        assertTrue("All entries should be filtered out", result.isEmpty())
    }

    @Test
    fun `category filter returns only requested category`() = runTest {
        val api = FakeApi(responses = mapOf(
            "^N225" to validQuote("^N225", 39000.0, 38500.0, "JPY"),
            "^DJI"  to validQuote("^DJI",  38000.0, 37800.0, "USD"),
            "BTC-USD" to validQuote("BTC-USD", 70000.0, 69000.0, "USD")
        ))
        val repo = MarketRepository(api)

        val jp = repo.fetchByCategory(MarketCategory.JP_STOCK)
        val us = repo.fetchByCategory(MarketCategory.US_STOCK)
        val crypto = repo.fetchByCategory(MarketCategory.CRYPTO)

        assertTrue(jp.all { it.category == MarketCategory.JP_STOCK })
        assertTrue(us.all { it.category == MarketCategory.US_STOCK })
        assertTrue(crypto.all { it.category == MarketCategory.CRYPTO })

        // 別カテゴリは混入しないこと
        assertFalse(jp.any { it.symbol == "^DJI" })
        assertFalse(us.any { it.symbol == "^N225" })
    }

    @Test
    fun `change calculations work for negative (down) movement`() = runTest {
        val api = FakeApi(responses = mapOf(
            "AAPL" to validQuote("AAPL", 180.0, 200.0, "USD")
        ))
        val repo = MarketRepository(api)
        val result = repo.fetchByCategory(MarketCategory.US_STOCK)

        val aapl = result.first { it.symbol == "AAPL" }
        assertEquals(-20.0, aapl.change, 0.001)
        assertEquals(-10.0, aapl.changePercent, 0.001)
        assertFalse(aapl.isUp)
        assertFalse(aapl.isFlat)
    }

    @Test
    fun `flat (no change) is correctly identified`() = runTest {
        val api = FakeApi(responses = mapOf(
            "^N225" to validQuote("^N225", 39000.0, 39000.0, "JPY")
        ))
        val repo = MarketRepository(api)
        val result = repo.fetchByCategory(MarketCategory.JP_STOCK)

        val n225 = result.first { it.symbol == "^N225" }
        assertEquals(0.0, n225.change, 0.001)
        assertTrue(n225.isFlat)
    }

    @Test
    fun `null previousClose falls back to chartPreviousClose`() = runTest {
        val response = YahooChartResponse(
            chart = YahooChart(result = listOf(
                YahooChartResult(meta = YahooMeta(
                    symbol = "AAPL",
                    currency = "USD",
                    regularMarketPrice = 200.0,
                    previousClose = null,
                    chartPreviousClose = 195.0
                ))
            ))
        )
        val api = FakeApi(responses = mapOf("AAPL" to response))
        val repo = MarketRepository(api)
        val result = repo.fetchByCategory(MarketCategory.US_STOCK)

        val aapl = result.first { it.symbol == "AAPL" }
        assertEquals(195.0, aapl.previousClose, 0.001)
    }

    @Test
    fun `fetchAll aggregates across all categories without crash on partial failure`() = runTest {
        val api = FakeApi(
            responses = mapOf(
                "^N225"   to validQuote("^N225",   39000.0, 38500.0, "JPY"),
                "^DJI"    to validQuote("^DJI",    38000.0, 37800.0, "USD"),
                "BTC-USD" to validQuote("BTC-USD", 70000.0, 69000.0, "USD")
            ),
            errors = setOf("^TPX", "AAPL", "ETH-USD")  // 一部失敗
        )
        val repo = MarketRepository(api)
        val all = repo.fetchAll()

        // 成功したものは含まれる
        assertTrue(all.any { it.symbol == "^N225" })
        assertTrue(all.any { it.symbol == "^DJI" })
        assertTrue(all.any { it.symbol == "BTC-USD" })
        // 失敗したものは除外
        assertFalse(all.any { it.symbol == "^TPX" })
        assertFalse(all.any { it.symbol == "AAPL" })
        assertFalse(all.any { it.symbol == "ETH-USD" })
    }
}
