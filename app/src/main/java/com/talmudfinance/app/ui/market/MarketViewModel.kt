package com.talmudfinance.app.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.talmudfinance.app.TalmudFinanceApp
import com.talmudfinance.app.data.model.MarketCategory
import com.talmudfinance.app.data.model.MarketQuote
import com.talmudfinance.app.data.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class MarketUiState(
    val isLoading: Boolean = true,
    val selectedCategory: MarketCategory = MarketCategory.JP_STOCK,
    val quotesByCategory: Map<MarketCategory, List<MarketQuote>> = emptyMap(),
    val lastUpdated: Instant? = null,
    val errorMessage: String? = null
)

class MarketViewModel(
    private val marketRepo: MarketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun selectCategory(category: MarketCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun loadAll() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val total = marketRepo.watchlist.size
            val result = runCatching { marketRepo.fetchAll() }
            result.fold(
                onSuccess = { quotes ->
                    val grouped = quotes.groupBy { it.category }
                    val errorMsg = when {
                        // 全銘柄失敗 → ほぼ確実にネットワーク障害
                        quotes.isEmpty() && total > 0 ->
                            "通信に失敗しました。ネットワーク接続を確認してください。"
                        // 半数以上失敗 → 部分的な不調を通知
                        quotes.size < total / 2 ->
                            "${total - quotes.size}件の取得に失敗しました。一部の銘柄が表示されない可能性があります。"
                        else -> null
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quotesByCategory = grouped,
                            lastUpdated = if (quotes.isNotEmpty()) Instant.now() else it.lastUpdated,
                            errorMessage = errorMsg
                        )
                    }
                },
                onFailure = { e ->
                    // fetchAll は通常例外を投げないが、安全網として残す
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "通信エラー")
                    }
                }
            )
        }
    }

    companion object {
        fun factory(app: TalmudFinanceApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MarketViewModel(app.marketRepository)
            }
        }
    }
}
