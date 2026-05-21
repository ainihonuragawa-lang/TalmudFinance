package com.talmudfinance.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * タルムードの教え（assets/talmud_teachings.json から読み込む）
 */
@Serializable
data class TalmudTeaching(
    val id: Int,
    val teaching: String,
    val source: String,
    val lesson: String,
    @SerialName("financial_interpretation") val financialInterpretation: String
)

@Serializable
data class TalmudCollection(
    val version: Int = 1,
    val note: String = "",
    val teachings: List<TalmudTeaching>
)
