package com.talmudfinance.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.talmudfinance.app.TalmudFinanceApp
import com.talmudfinance.app.data.model.MarketCategory
import com.talmudfinance.app.data.model.MarketQuote
import com.talmudfinance.app.data.model.TalmudTeaching
import com.talmudfinance.app.data.repository.MarketRepository
import com.talmudfinance.app.data.repository.TalmudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val teaching: TalmudTeaching? = null,
    val summaryQuotes: List<MarketQuote> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val marketRepo: MarketRepository,
    private val talmudRepo: TalmudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val teachingResult = runCatching { talmudRepo.getTodaysTeaching() }
            val quotesResult = runCatching { marketRepo.fetchAll() }

            val errorParts = mutableListOf<String>()
            teachingResult.exceptionOrNull()?.let { errorParts += "教え: ${it.message}" }
            quotesResult.exceptionOrNull()?.let { errorParts += "市場: ${it.message}" }

            // 全銘柄失敗（ネットワーク断の典型）も errorMessage に反映
            val all = quotesResult.getOrDefault(emptyList())
            if (all.isEmpty() && marketRepo.watchlist.isNotEmpty()) {
                errorParts += "市場データを取得できません。通信状態を確認してください。"
            }

            val summary = buildSummary(all)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    teaching = teachingResult.getOrNull(),
                    summaryQuotes = summary,
                    errorMessage = errorParts.joinToString(" / ").ifBlank { null }
                )
            }
        }
    }

    /** ホームのサマリー用に、各カテゴリの代表を1〜2件ずつピック */
    private fun buildSummary(all: List<MarketQuote>): List<MarketQuote> {
        val priorityPerCategory = mapOf(
            MarketCategory.JP_STOCK to listOf("^N225", "1306.T"),
            MarketCategory.US_STOCK to listOf("^GSPC", "^IXIC"),
            MarketCategory.FX to listOf("JPY=X"),
            MarketCategory.CRYPTO to listOf("BTC-USD")
        )
        val result = mutableListOf<MarketQuote>()
        priorityPerCategory.forEach { (_, symbols) ->
            symbols.forEach { sym ->
                all.firstOrNull { it.symbol == sym }?.let { result += it }
            }
        }
        return result
    }

    companion object {
        fun factory(app: TalmudFinanceApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(app.marketRepository, app.talmudRepository)
            }
        }
    }
}
