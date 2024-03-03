package com.mas.mobile.domain.message

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessageRuleServiceTest {
    private val mockMessageAnalyzer = mockk<MessageAnalyzer>(relaxed = true)
    private val mockMessageRuleRepository = mockk<MessageRuleRepository>(relaxed = true)

    private val underTest = MessageRuleService(mockMessageAnalyzer, mockMessageRuleRepository)

    @BeforeEach
    fun setUp() {
        every { mockMessageRuleRepository.create() } returns NEW_RULE
    }

    @Test
    fun `should update rule with new expenditure`() {
        val result = underTest.evaluateRuleChanges(MESSAGE_WITH_SAME_MERCHANT, RULE.copy(), NEW_EXPENDITURE)

        with(result!!) {
            assertEquals(RULE.id, id)
            assertEquals(SAME_MERCHANT, expenditureMatcher)
            assertEquals(NEW_EXPENDITURE, expenditureName)
        }
    }

    @Test
    fun `should create new rule with new merchant`() {
        val result = underTest.evaluateRuleChanges(MESSAGE_WITH_NEW_MERCHANT, RULE.copy(), NEW_EXPENDITURE)

        with(result!!) {
            assertEquals(NEW_RULE.id, id)
            assertEquals(NEW_MERCHANT, expenditureMatcher)
            assertEquals(NEW_EXPENDITURE, expenditureName)
        }
    }

    @Test
    fun `should skip no changes`() {
        assertNull(underTest.evaluateRuleChanges(MESSAGE_WITH_SAME_MERCHANT, RULE.copy(), SAME_EXPENDITURE))
    }

    private companion object {
        const val SAME_EXPENDITURE = "Food"
        const val NEW_EXPENDITURE = "Restaurants"
        const val SAME_MERCHANT = "McDonalds"
        const val NEW_MERCHANT = "PARKIGRUND"

        val RULE = MessageRule (
            id = MessageRuleId(1),
            name = "Sender",
            pattern = Pattern("of {amount} EUR in {merchant} with"),
            expenditureMatcher = SAME_MERCHANT,
            expenditureName = SAME_EXPENDITURE
        )

        val NEW_RULE = MessageRule (
            id = MessageRuleId(2),
            name = "",
            pattern = Pattern(),
            expenditureMatcher = "",
            expenditureName = ""
        )

        val MESSAGE_WITH_SAME_MERCHANT = Message(
            id = MessageId(1),
            sender = "Sender",
            text = "Purchase of 12.50 EUR in $SAME_MERCHANT with card ends 1234",
            status = Message.Matched(
                ruleId = RULE.id,
                suggestedAmount = 12.50,
                suggestedExpenditureName = null
            )
        )

        val MESSAGE_WITH_NEW_MERCHANT = Message(
            id = MessageId(1),
            sender = "Sender",
            text = "Purchase of 5.00 EUR in $NEW_MERCHANT with card ends 1234",
            status = Message.Matched(
                ruleId = RULE.id,
                suggestedAmount = 5.00,
                suggestedExpenditureName = null
            )
        )
    }
}