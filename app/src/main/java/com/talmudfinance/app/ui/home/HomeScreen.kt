package com.talmudfinance.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.talmudfinance.app.R
import com.talmudfinance.app.data.model.TalmudTeaching
import com.talmudfinance.app.ui.components.ErrorBox
import com.talmudfinance.app.ui.components.LoadingBox
import com.talmudfinance.app.ui.components.QuoteRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> LoadingBox()
        state.teaching == null && state.summaryQuotes.isEmpty() ->
            ErrorBox(state.errorMessage ?: "不明なエラー") { viewModel.load() }
        else -> HomeContent(
            teaching = state.teaching,
            summary = state.summaryQuotes,
            warning = state.errorMessage,
            onRefresh = viewModel::load
        )
    }
}

@Composable
private fun HomeContent(
    teaching: TalmudTeaching?,
    summary: List<com.talmudfinance.app.data.model.MarketQuote>,
    warning: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // ヘッダー
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(id = R.string.app_subtitle),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "再読み込み")
            }
        }
        Spacer(Modifier.height(16.dp))

        // 今日の教え
        teaching?.let { TeachingCard(it) }
        Spacer(Modifier.height(16.dp))

        // マーケットサマリー
        Text(
            stringResource(id = R.string.market_summary),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            summary.forEachIndexed { index, quote ->
                QuoteRow(quote = quote)
                if (index != summary.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            if (summary.isEmpty()) {
                Text(
                    "市場データを取得できませんでした。ネットワークをご確認ください。",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        warning?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TeachingCard(teaching: TalmudTeaching) {
    val today = LocalDate.now().format(
        DateTimeFormatter.ofPattern("yyyy年M月d日 (E)", Locale.JAPANESE)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                today,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                stringResource(id = R.string.todays_teaching),
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f))
            Text(
                "「${teaching.teaching}」",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "— ${teaching.source}",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(id = R.string.lesson_label) + ": ${teaching.lesson}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(id = R.string.financial_interpretation_label),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                teaching.financialInterpretation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
