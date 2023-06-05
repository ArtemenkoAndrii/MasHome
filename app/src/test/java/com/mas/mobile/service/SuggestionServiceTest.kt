package com.mas.mobile.service

import com.mas.mobile.domain.message.*
import com.mas.mobile.util.DateTool
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime


class SuggestionServiceTest {
    private val mockMessageRuleRepository = mockk<MessageRuleRepository>(relaxed = true)

    private val testInstance = SuggestionService(mockMessageRuleRepository)

    @Before
    fun setUp() {
        every { mockMessageRuleRepository.getAll() } returns listOf(NO_SUGGESTION.rule, AUTO_SUGGESTION.rule, MANUAL_SUGGESTION.rule)
    }

    @Test
    fun `should not return suggestion`() {
        NO_SUGGESTION.variants.forEach {
            val suggestion = testInstance.makeSuggestions(it.message)
            assertEquals(NoSuggestion, suggestion)
        }
    }

    @Test
    fun `should return auto suggestion`() {
        AUTO_SUGGESTION.variants.forEach {
            val suggestion = testInstance.makeSuggestions(it.message)
            assertEquals(it.suggestion, suggestion)
        }
    }

    @Test
    fun `should return manual suggestion`() {
        MANUAL_SUGGESTION.variants.forEach {
            val suggestion = testInstance.makeSuggestions(it.message)
            assertEquals(it.suggestion, suggestion)
        }
    }

    companion object {
        val NO_SUGGESTION = TestCase(
            rule = MessageRule(
                id = 0,
                name = "any",
                amountMatcher = "any",
                expenditureMatcher = "any",
                expenditureName = "any",
            ),
            variants = listOf(
                Variant(
                    message = Message(
                        sender = "unknown",
                        date = LocalDateTime.now(),
                        text = "01.05 Unknown message"
                    ),
                    suggestion = NoSuggestion
                )
            )
        )

        val AUTO_SUGGESTION = TestCase(
            rule = MessageRule(
                id = 1,
                name = "raiffeisen",
                amountMatcher = "poslugu {amount} UAH",
                expenditureMatcher = "Puzata Khata",
                expenditureName = "111",
            ),
            variants = listOf(
                // Standard case
                Variant(
                    message = Message(
                        sender = "raiffeisen",
                        date = DateTool.stringToDateTime("01.01 00:00"),
                        text = """
                    27.11.21 13:44 Mastecard
                    Platinum*6309 splata za tovar/
                    poslugu 440.00 UAH Puzata Khata.
                    Dostupna suma 98175.27 UAH
                """.trimIndent()
                    ),
                    suggestion = AutoSuggestion(
                        ruleId = 1,
                        expenditureName = "111",
                        amount = 440.00,
                        time = DateTool.stringToDateTime("01.01 00:00")
                    )
                ),
                // Sender contains a part of rule name
                Variant(
                    message = Message(
                        sender = "My raiffeisen sender",
                        date = DateTool.stringToDateTime("02.01 00:00"),
                        text = """
                    27.11.21 13:44 Mastecard
                    Platinum*6309 splata za tovar/
                    poslugu 440.00 UAH Puzata Khata.
                    Dostupna suma 98175.27 UAH
                """.trimIndent()
                    ),
                    suggestion = AutoSuggestion(
                        ruleId = 1,
                        expenditureName = "111",
                        amount = 440.00,
                        time = DateTool.stringToDateTime("02.01 00:00")
                    )
                ),
                // Amount with the comma
                Variant(
                    message = Message(
                        sender = "My raiffeisen sender",
                        date = DateTool.stringToDateTime("03.01 00:00"),
                        text = """
                    27.11.21 13:44 Mastecard
                    Platinum*6309 splata za tovar/
                    poslugu 440,00 UAH Puzata Khata.
                    Dostupna suma 98175.27 UAH
                """.trimIndent()
                    ),
                    suggestion = AutoSuggestion(
                        ruleId = 1,
                        expenditureName = "111",
                        amount = 440.00,
                        time = DateTool.stringToDateTime("03.01 00:00")
                    )
                )
            )
        )

        val MANUAL_SUGGESTION = TestCase(
            rule = MessageRule(
                id = 2,
                name = "Purchase accepted",
                amountMatcher = "Purchase of {amount} EUR",
                expenditureMatcher = "Unknown",
                expenditureName = "any",
            ),
            variants = listOf(
                // Expenditure doesn't match
                Variant(
                    message = Message(
                        sender = "Purchase accepted \uD83D\uDC4D\uD83D\uDCB3",
                        date = DateTool.stringToDateTime("04.01 00:00"),
                        text = """
                    Purchase of 3,01 EUR in aliexpress with your card ending in 8850.
                """.trimIndent()
                    ),
                    suggestion = ManualSuggestion(
                        ruleId = 2,
                        amount = 3.01,
                        time = DateTool.stringToDateTime("04.01 00:00")
                    )
                )
            )
        )
    }
}

data class TestCase(
    val rule: MessageRule,
    val variants: List<Variant>
)

data class Variant(
    val message: Message,
    val suggestion: Suggestion
)

