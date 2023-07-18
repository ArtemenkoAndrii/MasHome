package com.mas.mobile.service

import com.mas.mobile.domain.message.*
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class SuggestionServiceTest {
    private val mockMessageRuleRepository = mockk<MessageRuleRepository>(relaxed = true)
    private val testInstance = SuggestionService(mockMessageRuleRepository)

    @Before
    fun setUp() {
        every { mockMessageRuleRepository.getAll() } returns listOf(
            MessageRule(
                id = MessageRuleId(1),
                name = "Raiffeisen",
                amountMatcher = "poslugu {amount} UAH",
                expenditureMatcher = "Puzata Khata",
                expenditureName = "Food",
            ),
            MessageRule(
                id = MessageRuleId(2),
                name = "BBVA",
                amountMatcher = "Purchase of {amount} EUR",
                expenditureMatcher = "CARREF",
                expenditureName = "Food",
            )
        )
    }

    @Test
    fun `should match expenditure UAH`() {
        val result = testInstance.suggest(
            "Raiffeisen",
                   """
                    27.11.21 13:44 Mastecard
                    Platinum*6309 splata za tovar/
                    poslugu 440.00 UAH Puzata Khata.
                    Dostupna suma 98175.27 UAH
                   """.trimIndent()
        )

        assertTrue(result is Matched)
        assertEquals(MessageRuleId(1), (result as Matched).ruleId)
        assertEquals(440.00, result.amount, 0.001)
        assertEquals("Food", result.expenditureName)
    }

    @Test
    fun `should match expenditure EUR`() {
        val result = testInstance.suggest(
            "BBVA",
            "Purchase of 3,01 EUR in CARREF with your card ending in 8850."
        )

        assertTrue(result is Matched)
        assertEquals(MessageRuleId(2), (result as Matched).ruleId)
        assertEquals(3.01, result.amount, 0.001)
        assertEquals("Food", result.expenditureName)
    }

    @Test
    fun `should match match without expenditure`() {
        val result = testInstance.suggest(
            "BBVA",
            "Purchase of 3,01 EUR in aliexpress with your card ending in 8850."
        )

        assertTrue(result is Matched)
        assertEquals(MessageRuleId(2), (result as Matched).ruleId)
        assertEquals(3.01, result.amount, 0.001)
        assertNull(result.expenditureName)
    }

    @Test
    fun `should recommend`() {
        val result = testInstance.suggest(
            "Any random sender",
            "0.00 money ¥ any text."
        )

        assertTrue(result is Recommended)
    }

    @Test
    fun `should recommend with dot`() {
        val result = testInstance.suggest(
            "Any random sender",
            "Any random message with 100.00 number as EUR. Recommended"
        )

        assertTrue(result is Recommended)
    }

    @Test
    fun `should be rejected`() {
        val result = testInstance.suggest(
            "Any random sender",
            "Any random message"
        )

        assertTrue(result is Rejected)
    }
}

