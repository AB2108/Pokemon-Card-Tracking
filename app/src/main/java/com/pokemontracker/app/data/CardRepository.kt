package com.pokemontracker.app.data

import com.pokemontracker.app.util.ImageStorage
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for all data operations. Wraps the Room DAOs and keeps the
 * card + price-history relationship consistent (e.g. recording an initial value
 * when a card is created).
 */
class CardRepository(
    private val cardDao: CardDao,
    private val priceEntryDao: PriceEntryDao,
) {
    fun observeCardsWithValue(): Flow<List<CardWithValue>> = cardDao.observeCardsWithValue()

    fun observeCard(id: Long): Flow<Card?> = cardDao.observeCard(id)

    fun observeEntriesForCard(cardId: Long): Flow<List<PriceEntry>> =
        priceEntryDao.observeEntriesForCard(cardId)

    fun observeAllEntries(): Flow<List<PriceEntry>> = priceEntryDao.observeAllEntries()

    suspend fun getCard(id: Long): Card? = cardDao.getCard(id)

    /**
     * Insert a new card. When [initialValue] is provided it is stored as the
     * card's first price-history point at [date].
     */
    suspend fun addCard(card: Card, initialValue: Double?, date: Long): Long {
        val newId = cardDao.insert(card)
        if (initialValue != null) {
            priceEntryDao.insert(PriceEntry(cardId = newId, price = initialValue, date = date))
        }
        return newId
    }

    suspend fun updateCard(card: Card) = cardDao.update(card)

    /** Delete a card, its cascaded price history and its stored image file. */
    suspend fun deleteCard(card: Card) {
        cardDao.delete(card)
        card.imagePath?.let { ImageStorage.deleteImage(it) }
    }

    /** Append a new value to a card's price history. */
    suspend fun recordPrice(cardId: Long, price: Double, date: Long) {
        priceEntryDao.insert(PriceEntry(cardId = cardId, price = price, date = date))
    }

    suspend fun deletePriceEntry(entryId: Long) = priceEntryDao.deleteById(entryId)
}
