package com.talmudfinance.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * RetryInterceptor の振る舞いを MockWebServer で検証する。
 * テスト高速化のため initialDelayMs を短く設定。
 */
class RetryInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxRetries = 3, initialDelayMs = 10L, maxDelayMs = 50L))
            // OkHttp の自動再試行はオフにして、Interceptor の挙動だけを観測
            .retryOnConnectionFailure(false)
            .build()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    private fun execute(): okhttp3.Response {
        val req = Request.Builder().url(server.url("/")).build()
        return client.newCall(req).execute()
    }

    @Test
    fun `200_OK then no retry`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val response = execute()
        assertEquals(200, response.code)
        assertEquals(1, server.requestCount)  // リトライなし
        response.close()
    }

    @Test
    fun `400_BadRequest is returned as-is without retry`() {
        server.enqueue(MockResponse().setResponseCode(400))

        val response = execute()
        assertEquals(400, response.code)
        assertEquals(1, server.requestCount)  // 4xx はリトライしない
        response.close()
    }

    @Test
    fun `500_then_200 retries once and returns 200`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(200).setBody("recovered"))

        val response = execute()
        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)  // 1回失敗 + 1回成功
        response.close()
    }

    @Test
    fun `503_repeatedly returns 503 after max retries`() {
        repeat(10) { server.enqueue(MockResponse().setResponseCode(503)) }

        val response = execute()
        assertEquals(503, response.code)
        // maxRetries=3 なので最大4回試行（初回 + 3リトライ）
        assertEquals(4, server.requestCount)
        response.close()
    }

    @Test
    fun `429_TooManyRequests is retried`() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val response = execute()
        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }

    @Test
    fun `IOException via socket close is retried then succeeds`() {
        // 1回目はサーバが即切断 → IOException
        server.enqueue(MockResponse().setSocketPolicy(
            okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START
        ))
        // 2回目で200
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val response = execute()
        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
        response.close()
    }

    @Test
    fun `repeated IOException throws after exhausting retries`() {
        // すべての試行で接続失敗
        repeat(10) {
            server.enqueue(MockResponse().setSocketPolicy(
                okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START
            ))
        }

        try {
            execute().close()
            assertTrue("Expected IOException but call succeeded", false)
        } catch (e: IOException) {
            // 期待通り
            assertEquals(4, server.requestCount)  // 初回 + 3リトライ
        }
    }
}
