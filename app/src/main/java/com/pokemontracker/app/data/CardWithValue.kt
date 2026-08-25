package com.pokemontracker.app.data

import androidx.room.Embedded

/**
 * A [Card] together with its most recent recorded value, used for list and
 * overview screens. [currentValue] is null when no price has been recorded yet.
 */
data class CardWithValue(
    @Embedded val card: Card,
    val currentValue: Double?,
) {
    /** Current value multiplied by owned quantity. */
    val totalValue: Double
        get() = (currentValue ?: 0.0) * card.quantity
}
