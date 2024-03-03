package com.mas.mobile.domain.message

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PatternTest {
    @Test
    fun `should parse amount and expenditure`() {
        val result = Pattern("of {amount} EUR in {merchant} with")
            .parse("Payment accepted 👍💳 Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted.")

        with(result as Pattern.Data) {
            assertEquals(4.10, amount, 0.01)
            assertEquals("aliexpress", merchant)
        }
    }

    @Test
    fun `should parse amount only`() {
        val result = Pattern("of {amount} EUR in {merchant} with")
            .parse("Payment accepted 👍💳 Payment of 4,10 EUR in +800910000 info")

        with(result as Pattern.Data) {
            assertEquals(4.10, amount, 0.01)
            assertNull(merchant)
        }
    }

    @Test
    fun `should parse when merchant goes first`() {
        val result = Pattern("de {merchant} de {amount} EUR")
            .parse("Cargo de adeudo 📄 💰 Se ha cargado en tu cuenta 1077 un adeudo de ENDESA ENERGIA S.A. de 135,17 EUR")

        with(result as Pattern.Data) {
            assertEquals(135.17, amount, 0.001)
            assertEquals("ENDESA ENERGIA S.A.", merchant)
        }
    }

    @Test
    fun `should parse when a few amounts`() {
        val result = Pattern("Paid €{amount} at {merchant} Spent")
            .parse("AliExpress 🛍 Paid €3.77 at AliExpress Spent today: €100.00.")

        with(result as Pattern.Data) {
            assertEquals(3.77, amount, 0.001)
            assertEquals("AliExpress", merchant)
        }
    }

    @Test
    fun `should not parse if wrong template`() {
        val result = Pattern("of {amount} EUR in {merchant} with")
            .parse("Paid €3.77 at AliExpress Spent today: €100.00.")

        assertTrue(result is Pattern.Empty)
    }

    @Test
    fun `should not parse empty message`() {
        val result = Pattern("of {amount} EUR in {merchant} with")
            .parse("  ")

        assertTrue(result is Pattern.Empty)
    }

    @Test
    fun `should not parse empty pattern`() {
        val result = Pattern("")
            .parse("Paid €3.77 at AliExpress Spent today: €100.00.")

        assertTrue(result is Pattern.Empty)
    }
}