package com.pokemontracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Insert
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card): Int

    @Delete
    suspend fun delete(card: Card)

    @Query("SELECT * FROM cards WHERE id = :id")
    fun observeCard(id: Long): Flow<Card?>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCard(id: Long): Card?

    /**
     * All cards with their most recent value attached, ordered by name.
     * The correlated sub-query picks the newest [PriceEntry] per card.
     */
    @Query(
        """
        SELECT c.*,
            (SELECT p.price FROM price_entries p
             WHERE p.cardId = c.id
             ORDER BY p.date DESC, p.id DESC
             LIMIT 1) AS currentValue
        FROM cards c
        ORDER BY c.name COLLATE NOCASE ASC
        """
    )
    fun observeCardsWithValue(): Flow<List<CardWithValue>>
}
