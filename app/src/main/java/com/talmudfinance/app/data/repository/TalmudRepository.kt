package com.talmudfinance.app.data.repository

import android.content.Context
import com.talmudfinance.app.data.model.TalmudCollection
import com.talmudfinance.app.data.model.TalmudTeaching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId

/**
 * タルムードの教えを保持するリポジトリ。
 *
 * `jsonLoader` で JSON 文字列の取得を抽象化しているため、
 * 本番では Android Assets から、テストではメモリ上の文字列から読み込める。
 *
 * 「今日の教え」は日付ベースで決定論的に選ぶ。
 * 同じ日には同じ教えが返り、日付が変われば次の教えに進む。
 * 教えの数を超えた分は剰余で循環する。
 */
class TalmudRepository(
    private val jsonLoader: suspend () -> String
) {

    /** 本番用: Android Assets から読み込む */
    constructor(appContext: Context) : this({
        withContext(Dispatchers.IO) {
            appContext.assets.open("talmud_teachings.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        }
    })

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    @Volatile
    private var cache: TalmudCollection? = null

    /** 全件取得（キャッシュ付き） */
    suspend fun getAll(): List<TalmudTeaching> = loadCollection().teachings

    /** 今日の教えを取得（日付指定可・テスト向け） */
    suspend fun getTodaysTeaching(
        today: LocalDate = LocalDate.now(ZoneId.systemDefault())
    ): TalmudTeaching {
        val all = loadCollection().teachings
        require(all.isNotEmpty()) { "talmud_teachings.json に教えが入っていません" }
        // 日付（エポックからの日数）で決定論的に選ぶ
        val epochDay = today.toEpochDay()
        val idx = ((epochDay % all.size) + all.size).rem(all.size).toInt()
        return all[idx]
    }

    private suspend fun loadCollection(): TalmudCollection {
        cache?.let { return it }
        val text = jsonLoader()
        val parsed = json.decodeFromString(TalmudCollection.serializer(), text)
        cache = parsed
        return parsed
    }
}
