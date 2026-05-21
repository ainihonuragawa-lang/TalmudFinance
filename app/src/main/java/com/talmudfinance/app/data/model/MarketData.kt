package com.talmudfinance.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * UIで使う統一マーケットデータ
 */
data class MarketQuote(
    val symbol: String,
    val displayName: String,
    val category: MarketCategory,
    val price: Double,
    val previousClose: Double,
    val currency: String
) {
    val change: Double get() = price - previousClose
    val changePercent: Double get() = if (previousClose != 0.0) (change / previousClose) * 100.0 else 0.0
    val isUp: Boolean get() = change > 0
    val isFlat: Boolean get() = change == 0.0
}

enum class MarketCategory(val displayName: String) {
    JP_STOCK("日本株"),
    US_STOCK("米国株"),
    FX("為替"),
    CRYPTO("暗号資産")
}

/**
 * Yahoo Finance Chart API レスポンス
 * 例: https://query1.finance.yahoo.com/v8/finance/chart/^N225
 */
@Serializable
data class YahooChartResponse(
    val chart: YahooChart
)

@Serializable
data class YahooChart(
    val result: List<YahooChartResult>? = null,
    val error: YahooError? = null
)

@Serializable
data class YahooChartResult(
    val meta: YahooMeta
)

@Serializable
data class YahooMeta(
    val symbol: String,
    val currency: String? = null,
    @SerialName("regularMarketPrice") val regularMarketPrice: Double? = null,
    @SerialName("chartPreviousClose") val chartPreviousClose: Double? = null,
    @SerialName("previousClose") val previousClose: Double? = null,
    @SerialName("longName") val longName: String? = null,
    @SerialName("shortName") val shortName: String? = null
)

@Serializable
data class YahooError(
    val code: String? = null,
    val description: String? = null
)
