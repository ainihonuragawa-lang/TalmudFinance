package com.talmudfinance.app.ui.talmud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.talmudfinance.app.TalmudFinanceApp
import com.talmudfinance.app.data.model.TalmudTeaching
import com.talmudfinance.app.data.repository.TalmudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TalmudUiState(
    val isLoading: Boolean = true,
    val teachings: List<TalmudTeaching> = emptyList(),
    val errorMessage: String? = null
)

class TalmudViewModel(
    private val repo: TalmudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TalmudUiState())
    val uiState: StateFlow<TalmudUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val result = runCatching { repo.getAll() }
            result.fold(
                onSuccess = { list ->
                    _uiState.update { TalmudUiState(isLoading = false, teachings = list) }
                },
                onFailure = { e ->
                    _uiState.update {
                        TalmudUiState(isLoading = false, errorMessage = e.message ?: "読み込み失敗")
                    }
                }
            )
        }
    }

    companion object {
        fun factory(app: TalmudFinanceApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TalmudViewModel(app.talmudRepository)
            }
        }
    }
}
