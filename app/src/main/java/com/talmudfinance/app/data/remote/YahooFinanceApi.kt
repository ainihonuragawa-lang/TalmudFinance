package com.talmudfinance.app.data.remote

import com.talmudfinance.app.data.model.YahooChartResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Yahoo Finance の非公式 Chart API。
 * 無料・APIキー不要だが、利用規約上は商用利用は推奨されない。
 * MVP段階での動作確認用。本番リリース時は、Alpha Vantage / Twelve Data /
 * 各証券会社の公式APIへの差し替えを推奨。
 */
interface YahooFinanceApi {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getQuote(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "1d",
        @Query("range") range: String = "5d"
    ): YahooChartResponse
}
