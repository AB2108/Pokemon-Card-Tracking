package com.pokemontracker.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceEntryDao {

    @Insert
    suspend fun insert(entry: PriceEntry): Long

    @Query("SELECT * FROM price_entries WHERE cardId = :cardId ORDER BY date ASC, id ASC")
    fun observeEntriesForCard(cardId: Long): Flow<List<PriceEntry>>

    @Query("SELECT * FROM price_entries ORDER BY date ASC, id ASC")
    fun observeAllEntries(): Flow<List<PriceEntry>>

    @Query("DELETE FROM price_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}
