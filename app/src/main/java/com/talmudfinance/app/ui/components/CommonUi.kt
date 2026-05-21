package com.talmudfinance.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.talmudfinance.app.data.model.MarketQuote
import com.talmudfinance.app.ui.theme.MarketDown
import com.talmudfinance.app.ui.theme.MarketFlat
import com.talmudfinance.app.ui.theme.MarketUp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "データ取得に失敗しました",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("再試行") }
    }
}

/** 株価/レートの数値をカテゴリに応じてフォーマット */
fun formatPrice(quote: MarketQuote): String {
    val nf = NumberFormat.getNumberInstance(Locale.JAPAN)
    return when {
        quote.symbol.endsWith("=X") -> String.format(Locale.US, "%.4f", quote.price)
        quote.symbol.startsWith("BTC") || quote.symbol.startsWith("ETH") ||
                quote.symbol.startsWith("SOL") -> {
            nf.minimumFractionDigits = 2
            nf.maximumFractionDigits = 2
            nf.format(quote.price)
        }
        else -> {
            nf.minimumFractionDigits = 2
            nf.maximumFractionDigits = 2
            nf.format(quote.price)
        }
    }
}

fun changeColor(quote: MarketQuote): Color = when {
    quote.isFlat -> MarketFlat
    quote.isUp -> MarketUp
    else -> MarketDown
}

@Composable
fun QuoteRow(quote: MarketQuote) {
    val sign = when {
        quote.isFlat -> ""
        quote.isUp -> "+"
        else -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                quote.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                quote.symbol,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatPrice(quote),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                String.format(Locale.US, "%s%.2f (%s%.2f%%)",
                    sign, quote.change, sign, quote.changePercent),
                style = MaterialTheme.typography.labelLarge,
                color = changeColor(quote),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
