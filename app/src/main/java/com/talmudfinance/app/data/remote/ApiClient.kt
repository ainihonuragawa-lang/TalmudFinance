package com.talmudfinance.app.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val YAHOO_BASE = "https://query1.finance.yahoo.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            // 1. リトライ Interceptor を最初に（再試行ループ全体でロギングが回るよう順序重要）
            //    機内モード時の応答性を優先し、リトライ回数とディレイを短く設定
            .addInterceptor(RetryInterceptor(maxRetries = 2, initialDelayMs = 500L))
            // 2. ロギング（リトライ後の最終結果を記録）
            .addInterceptor(logging)
            // 3. User-Agent 付与
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (TalmudFinance Android)")
                    .build()
                chain.proceed(req)
            }
            // 機内モードや DNS 解決失敗時に応答を速くするため、タイムアウトを短縮
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            // OkHttp 既定の再試行は無効化（RetryInterceptor と二重になるのを防止）
            .retryOnConnectionFailure(false)
            .build()
    }

    val yahooFinanceApi: YahooFinanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(YAHOO_BASE)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(YahooFinanceApi::class.java)
    }
}
