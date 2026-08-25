package com.pokemontracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontracker.app.data.Card
import com.pokemontracker.app.data.CardRepository
import com.pokemontracker.app.data.PriceEntry
import com.pokemontracker.app.ui.components.ChartPoint
import com.pokemontracker.app.util.millisToDays
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs the card detail screen for a single card. */
class CardDetailViewModel(
    private val repository: CardRepository,
    private val cardId: Long,
) : ViewModel() {

    val card: StateFlow<Card?> =
        repository.observeCard(cardId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val entries: StateFlow<List<PriceEntry>> =
        repository.observeEntriesForCard(cardId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Most recent recorded value, or null if none. */
    val currentValue: StateFlow<Double?> =
        entries.map { it.maxByOrNull { e -> e.date }?.price }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Price history as chart points. */
    val priceSeries: StateFlow<List<ChartPoint>> =
        entries.map { list ->
            list.sortedBy { it.date }
                .map { ChartPoint(x = millisToDays(it.date), y = it.price.toFloat()) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun recordPrice(price: Double, date: Long) {
        viewModelScope.launch {
            repository.recordPrice(cardId, price, date)
        }
    }

    fun deletePriceEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deletePriceEntry(entryId)
        }
    }

    fun deleteCard(onDeleted: () -> Unit) {
        viewModelScope.launch {
            card.value?.let { repository.deleteCard(it) }
            onDeleted()
        }
    }
}
