package com.mas.mobile.domain.budget

import java.time.LocalDateTime

data class Spending(
    val id: SpendingId,
    var comment: String,
    var date: LocalDateTime,
    var amount: Double,
    var expenditure: Expenditure
)

@JvmInline
value class SpendingId(val value: Int)