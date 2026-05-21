package com.talmudfinance.app.ui.market

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.talmudfinance.app.data.model.MarketCategory
import com.talmudfinance.app.ui.components.ErrorBox
import com.talmudfinance.app.ui.components.LoadingBox
import com.talmudfinance.app.ui.components.QuoteRow
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MarketScreen(viewModel: MarketViewModel) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading && state.quotesByCategory.isEmpty() -> LoadingBox()
        state.errorMessage != null && state.quotesByCategory.isEmpty() ->
            ErrorBox(state.errorMessage ?: "") { viewModel.loadAll() }
        else -> MarketContent(state, viewModel::selectCategory, viewModel::loadAll)
    }
}

@Composable
private fun MarketContent(
    state: MarketUiState,
    onSelect: (MarketCategory) -> Unit,
    onRefresh: () -> Unit
) {
    val categories = MarketCategory.values().toList()
    val selectedIndex = categories.indexOf(state.selectedCategory).coerceAtLeast(0)
    val currentList = state.quotesByCategory[state.selectedCategory].orEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        // ヘッダー
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column {
                Text(
                    "マーケット",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                state.lastUpdated?.let { instant ->
                    val zdt = instant.atZone(ZoneId.systemDefault())
                    val formatted = DateTimeFormatter
                        .ofPattern("HH:mm:ss", Locale.JAPAN)
                        .format(zdt)
                    Text(
                        "最終更新: $formatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRefresh, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Filled.Refresh, contentDescription = "再読み込み")
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 16.dp
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { onSelect(cat) },
                    text = { Text(cat.displayName) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (currentList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "このカテゴリのデータがありません",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currentList, key = { it.symbol }) { quote ->
                    QuoteRow(quote)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}
