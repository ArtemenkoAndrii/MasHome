package com.mas.mobile

import com.mas.mobile.repository.MessageRuleRepository
import com.mas.mobile.repository.db.entity.ExpenditureData
import com.mas.mobile.repository.db.entity.MessageRule
import com.mas.mobile.repository.db.entity.MessageRuleData
import com.mas.mobile.service.*
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
        every { mockMessageRuleRepository.getBySender("unknown") } returns listOf()
        every { mockMessageRuleRepository.getBySender("raiffeisen") } returns listOf(AUTO_SUGGESTION.rule, MANUAL_SUGGESTION.rule)
    }

    @Test
    fun `should not return suggestion`() {
        val suggestion = testInstance.makeSuggestions(NO_SUGGESTION.message)
        assertEquals(NoSuggestion, suggestion)
    }

    @Test
    fun `should return auto suggestion`() {
        val suggestion = testInstance.makeSuggestions(AUTO_SUGGESTION.message)
        assertEquals(AUTO_SUGGESTION.suggestion, suggestion)
    }

//    @Test
//    fun `should return manual suggestion`() {
//        val suggestion = testInstance.makeSuggestions(MANUAL_SUGGESTION.message)
//        assertEquals(MANUAL_SUGGESTION.suggestion, suggestion)
//    }

    companion object {
        val NO_SUGGESTION = TestCase(
            rule = MessageRule(MessageRuleData(id = 1), ExpenditureData()),
            message = Message(
                sender = "unknown",
                date = LocalDateTime.now(),
                text = "01.05 Unknown message"
            ),
            suggestion = NoSuggestion
        )

        val AUTO_SUGGESTION = TestCase(
            rule = MessageRule(MessageRuleData(
                id = 2,
                name = "raiffeisen",
                amountMatcher = "poslugu {amount} UAH",
                expenditureMatcher = "Puzata Khata",
                expenditureId = 2),
                ExpenditureData()
            ),
            message = Message(
                sender = "raiffeisen",
                date = LocalDateTime.now(),
                text = """
                    27.11.21 13:44 Mastecard
                    Platinum*6309 splata za tovar/
                    poslugu 440.00 UAH Puzata Khata.
                    Dostupna suma 98175.27 UAH              
                """.trimIndent()
            ),
            suggestion = AutoSuggestion(
                ruleId = 2,
                expenditureId = 2,
                amount = 440.00
            )
        )

        val MANUAL_SUGGESTION = TestCase(
            rule = MessageRule(MessageRuleData(
                id = 3,
                name = "raiffeisen",
                amountMatcher = "poslugu {amount} UAH",
                expenditureMatcher = "",
                expenditureId = -1),
                ExpenditureData()
            ),
            message = Message(
                sender = "raiffeisen",
                date = LocalDateTime.now(),
                text = """
                    27.11.21 13:44 Mastecard
                    Platinum*6309 splata za tovar/
                    poslugu 21.90 UAH Epicentrk.
                    Dostupna suma 98175.27 UAH              
                """.trimIndent()
            ),
            suggestion = ManualSuggestion(
                ruleId = 3,
                amount = 21.90
            )
        )
    }
}

data class TestCase(
    val rule: MessageRule,
    val message: Message,
    val suggestion: Suggestion
)

