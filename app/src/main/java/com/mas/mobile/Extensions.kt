package com.mas.mobile

import java.util.Currency
import java.util.Locale

fun String.toCurrency(): Currency = try {
        Currency.getInstance(this)
    } catch (e: IllegalArgumentException) {
        Currency.getInstance(Locale.getDefault())
    }