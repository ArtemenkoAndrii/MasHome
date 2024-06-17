package com.mas.mobile

import com.mas.mobile.util.CurrencyTools
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale

fun String.toCurrency(): Currency = try {
        Currency.getInstance(this)
    } catch (e: IllegalArgumentException) {
        CurrencyTools.getDefaultCurrency()
    }

fun Double.halfEven() =
    BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_EVEN).toDouble()