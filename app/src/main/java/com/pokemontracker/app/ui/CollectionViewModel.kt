package com.pokemontracker.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontracker.app.data.CardRepository
import com.pokemontracker.app.data.CardWithValue
import com.pokemontracker.app.data.PriceEntry
import com.pokemontracker.app.ui.components.ChartPoint
import com.pokemontracker.app.util.millisToDays
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Backs the overview + collection list screens. */
class CollectionViewModel(repository: CardRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(value: String) {
        _query.value = value
    }

    private val allCards: StateFlow<List<CardWithValue>> =
        repository.observeCardsWithValue()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Cards filtered by the current search query. */
    val cards: StateFlow<List<CardWithValue>> =
        combine(allCards, _query) { list, q ->
            if (q.isBlank()) {
                list
            } else {
                list.filter { item ->
                    item.card.name.contains(q, ignoreCase = true) ||
                        item.card.setPack.contains(q, ignoreCase = true) ||
                        item.card.cardNumber.contains(q, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Current total value of the whole collection (value × quantity). */
    val totalValue: StateFlow<Double> =
        allCards.map { list -> list.sumOf { it.totalValue } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val cardCount: StateFlow<Int> =
        allCards.map { list -> list.sumOf { it.card.quantity } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Time series of the whole collection's value, for the overview chart. */
    val collectionSeries: StateFlow<List<ChartPoint>> =
        combine(allCards, repository.observeAllEntries()) { cards, entries ->
            buildCollectionSeries(cards, entries)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildCollectionSeries(
        cards: List<CardWithValue>,
        entries: List<PriceEntry>,
    ): List<ChartPoint> {
        if (entries.isEmpty()) return emptyList()

        val quantityByCard = cards.associate { it.card.id to it.card.quantity }
        val entriesByCard = entries.groupBy { it.cardId }
            .mapValues { (_, list) -> list.sortedBy { it.date } }
        val timeline = entries.map { it.date }.distinct().sorted()

        return timeline.map { t ->
            var total = 0.0
            for ((cardId, list) in entriesByCard) {
                val quantity = quantityByCard[cardId] ?: 1
                // Most recent recorded price on or before this timeline point.
                val price = list.lastOrNull { it.date <= t }?.price
                if (price != null) total += price * quantity
            }
            ChartPoint(x = millisToDays(t), y = total.toFloat())
        }
    }
}
