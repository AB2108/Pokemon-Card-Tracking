package com.pokemontracker.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

/** Formatting helpers. Values are always displayed in Euro. */
object Formatters {

    private val euroFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
        currency = Currency.getInstance("EUR")
    }

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
    private val shortDateFormat = SimpleDateFormat("dd.MM.yy", Locale.GERMANY)

    /** e.g. 12.5 -> "12,50 €". */
    fun money(value: Double?): String =
        if (value == null) "–" else euroFormat.format(value)

    /** e.g. epoch millis -> "25.08.2026". */
    fun date(epochMillis: Long): String = dateFormat.format(Date(epochMillis))

    /** e.g. epoch millis -> "25.08.26". */
    fun shortDate(epochMillis: Long): String = shortDateFormat.format(Date(epochMillis))
}

private const val MILLIS_PER_DAY = 86_400_000.0

/** Epoch millis -> fractional days since epoch, used as a chart x value. */
fun millisToDays(epochMillis: Long): Float = (epochMillis / MILLIS_PER_DAY).toFloat()

/** Chart x value (fractional days) -> epoch millis. */
fun daysToMillis(days: Float): Long = (days.toDouble() * MILLIS_PER_DAY).toLong()

/**
 * Parse a user-entered amount that may use either a comma or a dot as the
 * decimal separator. Returns null when the text is blank or not a number.
 */
fun parseAmount(text: String): Double? {
    val cleaned = text.trim().replace(" ", "").replace(",", ".")
    if (cleaned.isEmpty()) return null
    return cleaned.toDoubleOrNull()
}
