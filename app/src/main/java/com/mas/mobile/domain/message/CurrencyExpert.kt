package com.mas.mobile.domain.message

import java.util.Currency

object CurrencyExpert {
    fun detectCurrencies(text: String): List<Currency> =
        Currency.getAvailableCurrencies().filter { it.check(text) }.toList()

    private fun Currency.check(text: String) =
        symbol.toRegex().matches(text) || currencyCode.toRegex().matches(text)

    // Example: .*(\bEUR\b|\d+$EUR|EUR\d+).*
    private fun String.toRegex() =
        Regex(".*(\\b$this\\b|\\d+$this|$this\\d+).*", setOf(RegexOption.DOT_MATCHES_ALL))
}