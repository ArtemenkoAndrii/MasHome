package com.mas.mobile.service.domain.message

import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.domain.message.MessageRepository
import com.mas.mobile.domain.message.MessageRuleRepository
import com.mas.mobile.domain.message.MessageService
import com.mas.mobile.service.CoroutineService
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageServiceTest {
    private val mockCoroutineService = mockk<CoroutineService>(relaxed = true)
    private val mockRuleRepository = mockk<MessageRuleRepository>(relaxed = true)
    private val mockBudgetService = mockk<BudgetService>(relaxed = true)
    private val mockMessageRepository = mockk<MessageRepository>(relaxed = true)

    private val testInstance = MessageService(mockCoroutineService, mockRuleRepository, mockBudgetService, mockMessageRepository)

    @Before
    fun setUp() {

    }

    @Test
    fun `should classify as Recommended`() {
        val result = testInstance.classify("Bank", "Paid 10 EUR for McDonald's", emptyList())
        assertTrue(result is MessageService.Recommended)
    }

    @Test
    fun `should reject as no amount`() {
        val result = testInstance.classify("Bank", "Paid EUR for McDonald's", emptyList())
        assertTrue(result is MessageService.Rejected)
    }

    @Test
    fun `should reject as no currency`() {
        val result = testInstance.classify("Bank", "Paid 10 for McDonald's", emptyList())
        assertTrue(result is MessageService.Rejected)
    }

    @Test
    fun `should reject as no key word`() {
        val result = testInstance.classify("Bank", "Win 10 EUR for McDonald's", emptyList())
        assertTrue(result is MessageService.Rejected)
    }

    @Test
    fun `should reject as no key word(AD)`() {
        val result = testInstance.classify("Bank", "Grab free coins by checking in $. Make saving of $ 0,18 with 20 coins", emptyList())
        assertTrue(result is MessageService.Rejected)
    }

    @Test
    fun `should reject as no key word(Real estate)`() {
        val result = testInstance.classify("Bank",
            "Martha NEXT GUINDOS de 3 dormitorios esta a partir de 400€+iva",
            emptyList())
        assertTrue(result is MessageService.Rejected)
    }
}