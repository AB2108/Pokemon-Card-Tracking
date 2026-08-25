package com.pokemontracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokemontracker.app.data.PriceEntry
import com.pokemontracker.app.ui.CardDetailViewModel
import com.pokemontracker.app.ui.components.CardImage
import com.pokemontracker.app.ui.components.FullscreenImageViewer
import com.pokemontracker.app.ui.components.LineChart
import com.pokemontracker.app.util.Formatters
import com.pokemontracker.app.util.daysToMillis
import com.pokemontracker.app.util.parseAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val card by viewModel.card.collectAsStateWithLifecycle()
    val currentValue by viewModel.currentValue.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val series by viewModel.priceSeries.collectAsStateWithLifecycle()

    var showPriceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.name ?: "Karte") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    card?.let {
                        IconButton(onClick = { onEdit(it.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        val currentCard = card ?: return@Scaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Image + current value.
            Row(verticalAlignment = Alignment.CenterVertically) {
                CardImage(
                    imagePath = currentCard.imagePath,
                    modifier = Modifier
                        .width(140.dp)
                        .height(196.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = currentCard.imagePath != null) {
                            showFullscreen = true
                        },
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Aktueller Wert",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = Formatters.money(currentValue),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (currentCard.quantity > 1 && currentValue != null) {
                        Text(
                            text = "Gesamt (${currentCard.quantity}×): " +
                                Formatters.money(currentValue!! * currentCard.quantity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(onClick = { showPriceDialog = true }) {
                        Icon(Icons.Filled.Update, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Preis aktualisieren")
                    }
                }
            }

            // Tap the photo to view it full screen (with zoom).
            currentCard.imagePath?.let { path ->
                if (showFullscreen) {
                    FullscreenImageViewer(
                        imagePath = path,
                        onDismiss = { showFullscreen = false },
                    )
                }
            }

            // Attributes.
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    DetailRow("Sprache", currentCard.language)
                    DetailRow("Set / Pack", currentCard.setPack)
                    DetailRow("Kartennummer", currentCard.cardNumber)
                    DetailRow("Zustand", currentCard.condition)
                    DetailRow("Menge", currentCard.quantity.toString())
                    DetailRow(
                        "Kaufpreis",
                        currentCard.purchasePrice?.let { Formatters.money(it) } ?: "",
                    )
                    DetailRow("Notizen", currentCard.notes)
                    DetailRow("Hinzugefügt am", Formatters.date(currentCard.createdAt))
                }
            }

            // Price history chart.
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Preisverlauf", style = MaterialTheme.typography.titleMedium)
                    LineChart(
                        points = series,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        yLabel = { Formatters.money(it.toDouble()) },
                        xLabel = { Formatters.shortDate(daysToMillis(it)) },
                    )
                }
            }

            // Price history list.
            if (entries.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Einträge", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        entries.sortedByDescending { it.date }.forEachIndexed { index, entry ->
                            if (index > 0) HorizontalDivider()
                            PriceHistoryRow(
                                entry = entry,
                                canDelete = entries.size > 1,
                                onDelete = { viewModel.deletePriceEntry(entry.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPriceDialog) {
        RecordPriceDialog(
            onDismiss = { showPriceDialog = false },
            onConfirm = { amount ->
                viewModel.recordPrice(amount, System.currentTimeMillis())
                showPriceDialog = false
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Karte löschen?") },
            text = { Text("Die Karte und ihr gesamter Preisverlauf werden entfernt. Das kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCard(onDeleted = onBack)
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(130.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PriceHistoryRow(
    entry: PriceEntry,
    canDelete: Boolean,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = Formatters.date(entry.date),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = Formatters.money(entry.price),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eintrag löschen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun RecordPriceDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val amount = parseAmount(text)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preis aktualisieren") },
        text = {
            Column {
                Text(
                    "Trage den heutigen Wert der Karte ein. Der Verlauf bleibt erhalten.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Wert in €") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount?.let(onConfirm) },
                enabled = amount != null && amount >= 0,
            ) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
