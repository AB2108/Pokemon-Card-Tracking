package com.pokemontracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single Pokémon card in the collection.
 *
 * The card's *current* value is not stored on the entity itself. Instead every
 * value the user enters is recorded as a [PriceEntry] so that the full price
 * history is preserved and can be charted. The current value is simply the most
 * recent price entry for the card.
 */
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Card name, e.g. "Glurak" / "Charizard". */
    val name: String,

    /** Language of the card, e.g. "Deutsch", "Englisch", "Japanisch". */
    val language: String = "",

    /** Set / pack the card belongs to, e.g. "Base Set", "151". */
    val setPack: String = "",

    /** Collector number within the set, e.g. "4/102". */
    val cardNumber: String = "",

    /** Condition / grade, e.g. "Near Mint", "Mint". */
    val condition: String = "",

    /** How many copies of this card are owned. */
    val quantity: Int = 1,

    /** Original purchase price in EUR, if known. */
    val purchasePrice: Double? = null,

    /** Free-form notes. */
    val notes: String = "",

    /** Absolute path to the card photo stored in the app's private files dir. */
    val imagePath: String? = null,

    /** Creation timestamp (epoch millis). */
    val createdAt: Long = System.currentTimeMillis(),
)
