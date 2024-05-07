package com.mas.mobile.domain.message

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QualifierService @Inject constructor(
    private val qualifierRepository: QualifierRepository
) {
    fun isRecommended(text: String): Boolean {
        val lowerText = text.lowercase()
        return (lowerText.length < MESSAGE_LIMIT)
                && containsAmount(lowerText)
                && containsCurrency(lowerText)
                && matchesKeywords(lowerText)
    }

    private fun containsAmount(text: String) = AMOUNT_REGEX.containsMatchIn(text)

    private fun containsCurrency(text: String) =
        CURRENCIES.entries.any {
            text.contains(it.key, ignoreCase = true) || text.contains(it.value, ignoreCase = true)
        }

    private fun matchesKeywords(text: String): Boolean {
        val words = text.split(WORDS_REGEX)

        val skipWords = qualifierRepository.getSkipQualifiers().map { it.value.lowercase() }
        val catchWords = qualifierRepository.getCatchQualifiers().map { it.value.lowercase() }

        return !words.any { skipWords.contains(it) } && words.any { catchWords.contains(it) }
    }

    companion object {
        const val MESSAGE_LIMIT = 250

        val AMOUNT_REGEX = Regex(".*\\d+[.,]\\d+?.*")
        val WORDS_REGEX = "\\s+|\\p{Punct}".toRegex()

        val CURRENCIES = mutableMapOf(
            "UAH" to "₴",
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "JPY" to "¥",
            "AUD" to "$",
            "CAD" to "$",
            "CHF" to "Fr",
            "CNY" to "¥",
            "RUB" to "₽"
        ).also {
            val instance = Currency.getInstance(Locale.getDefault())
            if (instance != null) {
                it[instance.currencyCode.uppercase()] = instance.symbol
            }
        }
    }
}