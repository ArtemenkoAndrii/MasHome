package com.mas.mobile

import com.mas.mobile.util.CurrencyTools
import java.util.Currency
import java.util.Locale

fun String.toCurrency(): Currency = try {
        Currency.getInstance(this)
    } catch (e: IllegalArgumentException) {
        CurrencyTools.getDefaultCurrency()
    }