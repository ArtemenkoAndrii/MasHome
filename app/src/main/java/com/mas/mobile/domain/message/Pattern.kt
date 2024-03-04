package com.mas.mobile.domain.message

import android.util.Log

data class Pattern(val value: String = AMOUNT_PLACEHOLDER) {
    init {
        val hasAmount = value.contains(AMOUNT_PLACEHOLDER, ignoreCase = true)
        require(hasAmount) { "The pattern must have $AMOUNT_PLACEHOLDER placeholder" }
    }

    fun parse(message: String): Result {
        val amountIndex = value.indexOf(AMOUNT_PLACEHOLDER, ignoreCase = true)
        if (amountIndex == -1) {
            return Empty
        }

        val merchantIndex = value.indexOf(MERCHANT_PLACEHOLDER, ignoreCase = true)

        return value.find(message).toResult(amountFirst = amountIndex < merchantIndex)
            ?: value.cutToAmount().find(message).toResult()
            ?: return Empty
    }

    private fun String.find(text: String) = try {
        replace(AMOUNT_PLACEHOLDER, AMOUNT_REGEX, true)
            .replace(MERCHANT_PLACEHOLDER, MERCHANT_REGEX, true)
            .let { Regex(ANY_STRING_REGEX + it + ANY_STRING_REGEX) }
            .find(text)
    } catch (e: Throwable) {
        Log.i(this::class.java.name, "Matcher regex failed.", e)
        null
    }

    private fun String.cutToAmount(): String {
        val amountIndex = this.indexOf(AMOUNT_PLACEHOLDER)
        val merchantIndex = this.indexOf(MERCHANT_PLACEHOLDER)

        return if (amountIndex < merchantIndex) {
            this.substring(0, merchantIndex - 1)
        } else {
            this.substring(merchantIndex + MERCHANT_PLACEHOLDER.length,  this.length)
        }
    }

    private fun MatchResult?.toResult(amountFirst: Boolean = true) = this?.let { result ->
        when (result.groupValues.size) {
            2 -> Data(result.groupValues[1].asDouble(), null)
            3 -> {
                val (_, first, second) = result.groupValues
                val amount = if (amountFirst) first.asDouble() else second.asDouble()
                val merchant = if (amountFirst) second else first
                Data(amount, merchant)
            }
            else -> null
        }
    }

    private fun String?.asDouble() = this?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

    private companion object {
        const val AMOUNT_PLACEHOLDER = "{amount}"
        const val MERCHANT_PLACEHOLDER = "{merchant}"
        const val ANY_STRING_REGEX = ".*"
        const val AMOUNT_REGEX = "(\\d+(?:[.,]\\d+)?)"
        const val MERCHANT_REGEX = "($ANY_STRING_REGEX)"
    }

    sealed interface Result
    data class Data(val amount: Double, val merchant: String?) : Result
    object Empty : Result
}
