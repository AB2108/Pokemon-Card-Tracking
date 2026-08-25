package com.pokemontracker.app

import android.app.Application
import com.pokemontracker.app.data.AppDatabase
import com.pokemontracker.app.data.CardRepository

/**
 * Application subclass acting as a tiny service locator. Holds the singleton
 * [CardRepository] used by all view models.
 */
class PokemonTrackerApp : Application() {

    val repository: CardRepository by lazy {
        val db = AppDatabase.getInstance(this)
        CardRepository(db.cardDao(), db.priceEntryDao())
    }
}
