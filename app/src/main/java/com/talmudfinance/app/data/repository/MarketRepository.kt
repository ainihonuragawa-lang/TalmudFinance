package com.talmudfinance.app.data.repository

import com.talmudfinance.app.data.model.MarketCategory
import com.talmudfinance.app.data.model.MarketQuote
import com.talmudfinance.app.data.remote.ApiClient
import com.talmudfinance.app.data.remote.YahooFinanceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * マーケットデータの取得を司るリポジトリ。
 * Yahoo Finance Chart API を並列で叩き、UI 用の MarketQuote へ正規化する。
 */
class MarketRepository(
    private val api: YahooFinanceApi = ApiClient.yahooFinanceApi
) {

    data class SymbolDef(
        val symbol: String,
        val displayName: String,
        val category: MarketCategory
    )

    /** MVPで取得する銘柄の固定リスト */
    val watchlist: List<SymbolDef> = listOf(
        // 日本株 (指数 + 代表的個別)
        SymbolDef("^N225",   "日経平均株価",      MarketCategory.JP_STOCK),
        // TOPIX 指数自体は Yahoo Finance の query API では引けないため、
        // 国内最大級の流動性を持つ TOPIX 連動 ETF（1306.T）で代替
        SymbolDef("1306.T",  "TOPIX (ETF連動)",   MarketCategory.JP_STOCK),
        SymbolDef("7203.T",  "トヨタ自動車",      MarketCategory.JP_STOCK),
        SymbolDef("6758.T",  "ソニーグループ",    MarketCategory.JP_STOCK),
        SymbolDef("9984.T",  "ソフトバンクG",    MarketCategory.JP_STOCK),

        // 米国株 (指数 + 代表的個別)
        SymbolDef("^DJI",    "NYダウ",            MarketCategory.US_STOCK),
        SymbolDef("^GSPC",   "S&P 500",          MarketCategory.US_STOCK),
        SymbolDef("^IXIC",   "NASDAQ総合",       MarketCategory.US_STOCK),
        SymbolDef("AAPL",    "Apple",            MarketCategory.US_STOCK),
        SymbolDef("MSFT",    "Microsoft",        MarketCategory.US_STOCK),
        SymbolDef("NVDA",    "NVIDIA",           MarketCategory.US_STOCK),

        // 為替
        SymbolDef("JPY=X",      "USD / JPY",     MarketCategory.FX),
        SymbolDef("EURJPY=X",   "EUR / JPY",     MarketCategory.FX),
        SymbolDef("EURUSD=X",   "EUR / USD",     MarketCategory.FX),
        SymbolDef("GBPJPY=X",   "GBP / JPY",     MarketCategory.FX),

        // 暗号資産
        SymbolDef("BTC-USD",    "Bitcoin",       MarketCategory.CRYPTO),
        SymbolDef("ETH-USD",    "Ethereum",      MarketCategory.CRYPTO),
        SymbolDef("SOL-USD",    "Solana",        MarketCategory.CRYPTO),
        SymbolDef("BTC-JPY",    "Bitcoin (JPY)", MarketCategory.CRYPTO)
    )

    /**
     * 全銘柄を並列取得。失敗した銘柄は null を返して落とす。
     */
    suspend fun fetchAll(): List<MarketQuote> = withContext(Dispatchers.IO) {
        coroutineScope {
            watchlist.map { def ->
                async { fetchOne(def) }
            }.awaitAll().filterNotNull()
        }
    }

    /** カテゴリ別取得 */
    suspend fun fetchByCategory(category: MarketCategory): List<MarketQuote> =
        withContext(Dispatchers.IO) {
            coroutineScope {
                watchlist
                    .filter { it.category == category }
                    .map { def -> async { fetchOne(def) } }
                    .awaitAll()
                    .filterNotNull()
            }
        }

    private suspend fun fetchOne(def: SymbolDef): MarketQuote? {
        return runCatching {
            val res = api.getQuote(def.symbol)
            val meta = res.chart.result?.firstOrNull()?.meta ?: return@runCatching null
            val price = meta.regularMarketPrice ?: return@runCatching null
            val prev = meta.previousClose ?: meta.chartPreviousClose ?: price
            MarketQuote(
                symbol = def.symbol,
                displayName = def.displayName,
                category = def.category,
                price = price,
                previousClose = prev,
                currency = meta.currency ?: ""
            )
        }.getOrNull()
    }
}
