package com.talmudfinance.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

/**
 * 指数バックオフ付きリトライ Interceptor。
 *
 * リトライ対象:
 *   - IOException（ネットワーク失敗、タイムアウト、接続断）
 *   - HTTP 5xx レスポンス（サーバ側エラー）
 *   - HTTP 429（Too Many Requests）
 *
 * リトライしない:
 *   - HTTP 2xx 成功
 *   - HTTP 4xx クライアントエラー（429除く）：何度試しても結果は同じ
 *
 * 待機時間: initialDelayMs × 2^(attempt-1) + ジッタ(0〜300ms)
 *   例: initialDelay=1000ms, maxRetries=3 → 試行間隔 ≈ 1s, 2s, 4s
 *
 * @param maxRetries 再試行回数（初回送信は含まない）
 * @param initialDelayMs 1回目リトライ前の待機時間（ms）
 * @param maxDelayMs バックオフの上限（ms）
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 8000L
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastResponse: Response? = null
        var lastException: IOException? = null

        var attempt = 0
        while (attempt <= maxRetries) {
            // 2回目以降は待機
            if (attempt > 0) {
                val baseDelay = initialDelayMs shl (attempt - 1)  // 2^(attempt-1) 倍
                val cappedDelay = min(baseDelay, maxDelayMs)
                val jitter = Random.nextLong(0, 301)             // 0〜300ms
                val delay = cappedDelay + jitter

                try {
                    Thread.sleep(delay)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Retry interrupted before attempt ${attempt + 1}", e)
                }

                // 前回の Response が残っていればボディを解放（リソースリーク防止）
                lastResponse?.close()
                lastResponse = null
            }

            lastException = null
            try {
                val response = chain.proceed(request)

                // 成功 or リトライ不要な 4xx（429除く） → 即返す
                if (response.isSuccessful) return response
                if (response.code in 400..499 && response.code != 429) return response

                // 5xx または 429 → リトライ対象
                lastResponse = response
            } catch (e: IOException) {
                // ネットワーク系失敗 → リトライ対象
                lastException = e
            }
            attempt++
        }

        // すべての試行で失敗
        return lastResponse ?: throw (lastException
            ?: IOException("RetryInterceptor: all $maxRetries retries exhausted"))
    }
}
