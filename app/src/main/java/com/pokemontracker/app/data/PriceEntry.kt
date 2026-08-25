package com.pokemontracker.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single point in a card's price history. One entry is created each time the
 * user records or updates a card's value, forming the time series that powers
 * the per-card and collection price charts.
 */
@Entity(
    tableName = "price_entries",
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("cardId")],
)
data class PriceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Owning card. */
    val cardId: Long,

    /** Recorded value in EUR. */
    val price: Double,

    /** When this value was recorded (epoch millis). */
    val date: Long = System.currentTimeMillis(),
)
