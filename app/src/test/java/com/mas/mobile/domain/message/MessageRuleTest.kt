package com.mas.mobile.domain.message

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Currency

class MessageRuleTest {
    private val mockPattern = mockk<Pattern>()

    @BeforeEach
    fun setUp() {
        every { mockPattern.parse(any()) } returns AMOUNT_AND_MERCHANT
    }

    @Test
    fun `should match amount and expenditure`() {
        val result = MessageRule(
            id = MessageRuleId(0),
            name = "BBVA",
            pattern = mockPattern,
            expenditureMatcher = "aliexpress",
            expenditureName = "Other",
            currency = CURRENCY
        ).evaluate(
            "BBVA",
            "Payment accepted 👍💳 Payment of 1,00 EUR in aliexpress with your card ending in 8850 accepted."
        )

        with (result as MessageRule.Match) {
            assertEquals(AMOUNT_AND_MERCHANT.amount, amount, 0.001)
            assertEquals(AMOUNT_AND_MERCHANT.merchant, merchant)
            assertEquals("Other", expenditureName)
        }
    }

    @Test
    fun `should match amount only`() {
        every { mockPattern.parse(any()) } returns ONLY_AMOUNT

        val result = MessageRule(
            id = MessageRuleId(0),
            name = "BBVA",
            pattern = mockPattern,
            expenditureMatcher = "aliexpress",
            expenditureName = "Other",
            currency = CURRENCY
        ).evaluate(
            "BBVA",
            "Payment accepted 👍💳 Payment of 2,00 EUR in +800910000 info"
        )

        with (result as MessageRule.Match) {
            assertEquals(ONLY_AMOUNT.amount, amount, 0.001)
            assertNull(merchant)
            assertNull(expenditureName)
        }
    }

    @Test
    fun `should match ignoring expenditure case`() {
        val result = MessageRule(
            id = MessageRuleId(0),
            name = "bbva",
            pattern = mockPattern,
            expenditureMatcher = "AliExpress",
            expenditureName = "Other",
            currency = CURRENCY
        ).evaluate(
            "BBVA",
            "Payment accepted 👍💳 Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted."
        )

        with (result as MessageRule.Match) {
            assertEquals(AMOUNT_AND_MERCHANT.amount, amount, 0.001)
            assertEquals(AMOUNT_AND_MERCHANT.merchant, merchant)
            assertEquals("Other", expenditureName)
        }
    }

    @Test
    fun `should not match sender`() {
        val result = MessageRule(
            id = MessageRuleId(0),
            name = "Privat",
            pattern = mockPattern,
            expenditureMatcher = "AliExpress",
            expenditureName = "Other",
            currency = CURRENCY
        ).evaluate(
            "BBVA",
            "Payment accepted 👍💳 Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted."
        )

        assertTrue(result is MessageRule.NoMatch)
    }

    @Test
    fun `should not match pattern`() {
        every { mockPattern.parse(any()) } returns EMPTY

        val result = MessageRule(
            id = MessageRuleId(0),
            name = "BBVA",
            pattern = mockPattern,
            expenditureMatcher = "AliExpress",
            expenditureName = "Other",
            currency = CURRENCY
        ).evaluate(
            "BBVA",
            "Payment accepted 👍💳 Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted."
        )

        assertTrue(result is MessageRule.NoMatch)
    }

    @Test
    fun `should not match spam`() {
        every { mockPattern.parse(any()) } returns EMPTY

        val result = MessageRule(
            id = MessageRuleId(0),
            name = "Openbank",
            pattern = mockPattern,
            expenditureMatcher = "AliExpress",
            expenditureName = "Other",
            currency = CURRENCY
        ).evaluate(
            "Openbank",
            "¡Aún estás a tiempo! Publi: Sorteamos 5 reembolsos de hasta 1.000 € para las compras y pagos aplazados (mín. 30 €) que realices entre el 19/12/23 al 19/12/24 (ambos incl.)"
        )

        assertTrue(result is MessageRule.NoMatch)
    }

    private companion object {
        val EMPTY = Pattern.Empty
        val AMOUNT_AND_MERCHANT = Pattern.Data(1.00, "merchant", Pattern.Indexes(-1,-1,-1,-1))
        val ONLY_AMOUNT = Pattern.Data(2.00, null, Pattern.Indexes(-1,-1,-1,-1))
        val CURRENCY = Currency.getInstance("EUR")
    }
}