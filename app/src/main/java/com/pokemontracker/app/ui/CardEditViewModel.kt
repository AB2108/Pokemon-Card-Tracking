package com.pokemontracker.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokemontracker.app.data.Card
import com.pokemontracker.app.data.CardRepository
import com.pokemontracker.app.util.ImageStorage
import com.pokemontracker.app.util.parseAmount
import kotlinx.coroutines.launch

/**
 * Backs the add / edit card form. When [cardId] is null the form creates a new
 * card (and an optional initial value); otherwise it edits the existing card's
 * attributes. Value updates for existing cards happen on the detail screen so
 * that price history stays clean.
 */
class CardEditViewModel(
    private val repository: CardRepository,
    private val cardId: Long?,
) : ViewModel() {

    val isEditing: Boolean = cardId != null

    var name by mutableStateOf("")
    var language by mutableStateOf("")
    var setPack by mutableStateOf("")
    var cardNumber by mutableStateOf("")
    var condition by mutableStateOf("")
    var quantity by mutableStateOf("1")
    var purchasePrice by mutableStateOf("")
    var notes by mutableStateOf("")
    var initialValue by mutableStateOf("")
    var imagePath by mutableStateOf<String?>(null)

    private var loaded = false
    private var originalImagePath: String? = null

    init {
        if (cardId != null) {
            viewModelScope.launch {
                repository.getCard(cardId)?.let { populateFrom(it) }
            }
        }
    }

    private fun populateFrom(card: Card) {
        name = card.name
        language = card.language
        setPack = card.setPack
        cardNumber = card.cardNumber
        condition = card.condition
        quantity = card.quantity.toString()
        purchasePrice = card.purchasePrice?.let { formatPlain(it) } ?: ""
        notes = card.notes
        imagePath = card.imagePath
        originalImagePath = card.imagePath
        loaded = true
    }

    val canSave: Boolean
        get() = name.isNotBlank()

    /** Update the selected image, cleaning up a previously chosen (unsaved) one. */
    fun onImageSelected(newPath: String?) {
        val previous = imagePath
        // Only delete a path we created during this editing session, never the
        // originally saved image (that is handled on save).
        if (previous != null && previous != originalImagePath) {
            ImageStorage.deleteImage(previous)
        }
        imagePath = newPath
    }

    fun save(onSaved: (Long) -> Unit) {
        if (!canSave) return
        val qty = quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val purchase = parseAmount(purchasePrice)

        viewModelScope.launch {
            if (cardId == null) {
                val newCard = Card(
                    name = name.trim(),
                    language = language.trim(),
                    setPack = setPack.trim(),
                    cardNumber = cardNumber.trim(),
                    condition = condition.trim(),
                    quantity = qty,
                    purchasePrice = purchase,
                    notes = notes.trim(),
                    imagePath = imagePath,
                )
                val id = repository.addCard(
                    card = newCard,
                    initialValue = parseAmount(initialValue),
                    date = System.currentTimeMillis(),
                )
                onSaved(id)
            } else {
                val existing = repository.getCard(cardId) ?: return@launch
                val updated = existing.copy(
                    name = name.trim(),
                    language = language.trim(),
                    setPack = setPack.trim(),
                    cardNumber = cardNumber.trim(),
                    condition = condition.trim(),
                    quantity = qty,
                    purchasePrice = purchase,
                    notes = notes.trim(),
                    imagePath = imagePath,
                )
                repository.updateCard(updated)
                // The original image was replaced -> free the old file.
                val old = originalImagePath
                if (old != null && old != imagePath) {
                    ImageStorage.deleteImage(old)
                }
                originalImagePath = imagePath
                onSaved(cardId)
            }
        }
    }

    private fun formatPlain(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}
