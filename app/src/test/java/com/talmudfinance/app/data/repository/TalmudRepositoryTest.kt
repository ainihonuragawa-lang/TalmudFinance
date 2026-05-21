package com.talmudfinance.app.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * TalmudRepository のユニットテスト。
 * JSON loader を差し替えることで、アセットに依存せずに検証できる。
 */
class TalmudRepositoryTest {

    private val sampleJson = """
        {
          "version": 1,
          "note": "test",
          "teachings": [
            {"id":1, "teaching":"A", "source":"src1", "lesson":"lA", "financial_interpretation":"fA"},
            {"id":2, "teaching":"B", "source":"src2", "lesson":"lB", "financial_interpretation":"fB"},
            {"id":3, "teaching":"C", "source":"src3", "lesson":"lC", "financial_interpretation":"fC"}
          ]
        }
    """.trimIndent()

    private fun repoOf(json: String) = TalmudRepository(jsonLoader = { json })

    @Test
    fun `getAll returns all teachings`() = runTest {
        val repo = repoOf(sampleJson)
        val all = repo.getAll()
        assertEquals(3, all.size)
        assertEquals(1, all[0].id)
        assertEquals("B", all[1].teaching)
        assertEquals("fC", all[2].financialInterpretation)
    }

    @Test
    fun `getTodaysTeaching is deterministic for the same date`() = runTest {
        val repo = repoOf(sampleJson)
        val date = LocalDate.of(2026, 5, 15)
        val first = repo.getTodaysTeaching(date)
        val second = repo.getTodaysTeaching(date)
        assertEquals(first.id, second.id)
    }

    @Test
    fun `getTodaysTeaching advances on next day`() = runTest {
        val repo = repoOf(sampleJson)
        val day1 = repo.getTodaysTeaching(LocalDate.of(2026, 5, 15))
        val day2 = repo.getTodaysTeaching(LocalDate.of(2026, 5, 16))
        // 3件しかないので day+1 では別のものになる
        assertNotEquals(day1.id, day2.id)
    }

    @Test
    fun `getTodaysTeaching cycles after exceeding teaching count`() = runTest {
        val repo = repoOf(sampleJson)
        val base = LocalDate.of(2026, 5, 15)
        val baseTeaching = repo.getTodaysTeaching(base)
        // 3件あるので 3日後に同じ教えに戻るはず
        val threeDaysLater = repo.getTodaysTeaching(base.plusDays(3))
        assertEquals(baseTeaching.id, threeDaysLater.id)
    }

    @Test
    fun `getTodaysTeaching works for dates before 1970 (negative epoch day)`() = runTest {
        val repo = repoOf(sampleJson)
        // 1969年 → toEpochDay() が負になるが、剰余演算で正規化されていること
        val result = repo.getTodaysTeaching(LocalDate.of(1969, 1, 1))
        assertTrue(result.id in 1..3)
    }

    @Test
    fun `loadCollection caches and does not re-invoke jsonLoader`() = runTest {
        var callCount = 0
        val repo = TalmudRepository(jsonLoader = {
            callCount++
            sampleJson
        })
        repo.getAll()
        repo.getAll()
        repo.getTodaysTeaching(LocalDate.of(2026, 5, 15))
        // キャッシュにより jsonLoader は1回しか呼ばれない
        assertEquals(1, callCount)
    }

    @Test
    fun `empty teachings list throws IllegalArgumentException`() = runTest {
        val emptyJson = """{"version":1, "note":"", "teachings":[]}"""
        val repo = repoOf(emptyJson)
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { repo.getTodaysTeaching(LocalDate.now()) }
        }
    }

    @Test
    fun `unknown JSON keys are ignored`() = runTest {
        val extraKeysJson = """
            {
              "version": 1,
              "note": "",
              "unknown_field": "should be ignored",
              "teachings": [
                {"id":1, "teaching":"X", "source":"s", "lesson":"l", "financial_interpretation":"f", "extra":"ignored"}
              ]
            }
        """.trimIndent()
        val repo = repoOf(extraKeysJson)
        val all = repo.getAll()
        assertEquals(1, all.size)
        assertEquals("X", all[0].teaching)
    }
}
