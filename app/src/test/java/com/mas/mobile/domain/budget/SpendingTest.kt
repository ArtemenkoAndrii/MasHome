package com.mas.mobile.domain.budget

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Currency
import kotlin.test.assertEquals

class SpendingTest {
    @Test
    fun `should exchange amout` () {
        SPENDING.exchange()
        assertEquals(20.00, SPENDING.amount, 0.001)
    }

    private companion object {
        val EXPENDITURE = Expenditure(
            ExpenditureId(2),
            "Food",
            CategoryId(0),
            100.00,
            0.00,
            "",
            BudgetId(0)
        )

        val SPENDING = Spending(
            SpendingId(1),
            "McDonalds",
            LocalDateTime.now(),
            0.00,
            EXPENDITURE,
            exchangeInfo = ExchangeInfo(
                rawAmount = 10.00,
                rate = 2.00,
                currency = Currency.getInstance("EUR")
            )
        )
    }

}