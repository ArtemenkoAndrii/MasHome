package com.mas.mobile.domain.budget

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.Currency

data class Spending(
    val id: SpendingId,
    var comment: String,
    var date: LocalDateTime,
    var amount: Double,
    var expenditure: Expenditure,
    var exchangeInfo: ExchangeInfo? = null
) {

    fun exchange() = exchangeInfo?.let { method ->
        amount = BigDecimal(method.rawAmount * method.rate)
            .setScale(2, RoundingMode.HALF_EVEN)
            .toDouble()
    }
}

data class ExchangeInfo(
    var rawAmount: Double,
    var rate: Double,
    var currency: Currency
)

@JvmInline
value class SpendingId(val value: Int)