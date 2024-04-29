package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.BudgetRepository
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.domain.budget.ExchangeRepository
import com.mas.mobile.domain.budget.Expenditure
import com.mas.mobile.domain.budget.ExpenditureRepository
import com.mas.mobile.domain.budget.Spending
import com.mas.mobile.domain.budget.SpendingId
import com.mas.mobile.domain.budget.SpendingRepository
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.service.ErrorHandler
import com.mas.mobile.service.ResourceService
import com.mas.mobile.service.TaskService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Currency

class MessageServiceTest {
    private val mockMessageRuleRepository = mockk<MessageRuleRepository>(relaxed = true)
    private val mockQualifierService = mockk<QualifierService>(relaxed = true)
    private val mockMessageRepository = mockk<MessageRepository>(relaxed = true)
    private val mockExchangeRepository = mockk<ExchangeRepository>(relaxed = true)

    // BudgetService mockk doesn't work because of bug with mocking Int value classes
    private val budgetService = BudgetService(
        mockk<ResourceService>(relaxed = true),
        mockk<SettingsRepository>(relaxed = true),
        mockk<SpendingRepository>(relaxed = true) {
            every { create() } returns Spending(
                SPENDING_ID, "", TIME_NOW, 0.0, mockk<Expenditure>(relaxed = true), null
            )
        },
        mockExchangeRepository,
        mockk<ErrorHandler>(relaxed = true),
        mockk<BudgetRepository>(relaxed = true),
        mockk<ExpenditureRepository>(relaxed = true)
    )

    private val message = slot<Message>()

    private val testInstance = MessageService(
        DummyTaskService,
        mockMessageRuleRepository,
        budgetService,
        mockQualifierService,
        mockMessageRepository)

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0

        every { mockMessageRuleRepository.getAll() } returns listOf(RULE)
        every { mockMessageRepository.create() } returns MESSAGE
        every { mockQualifierService.isRecommended(any()) } returns true

        coEvery { mockMessageRepository.save(capture(message)) } returns Unit
        coEvery { mockExchangeRepository.getRate(any(), any()) } returns Result.success(RATE)
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `should handle matched`() {
        testInstance.handleMessage(
            "Revolut",
            "AliExpress 🛍 Paid €3.77 at AliExpress Spent today: €100.00.",
            TIME_NOW
        )

        val result = message.captured
        assertEquals("Revolut", result.sender)
        assertEquals("AliExpress 🛍 Paid €3.77 at AliExpress Spent today: €100.00.", result.text)
        assertTrue(result.isNew)
        with(result.status as Message.Matched) {
            assertEquals(1, ruleId.value)
            assertEquals(3.77, suggestedAmount, 0.001)
            assertEquals("Other", suggestedExpenditureName)
        }
    }

    @Test
    fun `should handle recommended`() {
        testInstance.handleMessage(
            "BBVA",
            "Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted.",
            TIME_NOW
        )

        val result = message.captured
        assertEquals("BBVA", result.sender)
        assertEquals("Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted.", result.text)
        assertTrue(result.isNew)
        assertTrue(result.status is Message.Recommended)
    }

    @Test
    fun `should reject`() {
        every { mockQualifierService.isRecommended(any()) } returns false

        testInstance.handleMessage(
            "BBVA",
            "Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted.",
            TIME_NOW
        )

        coVerify(exactly = 0) { mockMessageRepository.save(any()) }
    }

    private companion object {
        val TIME_NOW: LocalDateTime = LocalDateTime.now()
        val RATE: Double = 1.0
        val RULE_ID = MessageRuleId(1)
        val MESSAGE_ID = MessageId(1)
        val SPENDING_ID = SpendingId(1)
        val CURRENCY: Currency = Currency.getInstance("EUR")
        val RULE = MessageRule(
            id = RULE_ID,
            name = "Revolut",
            pattern = Pattern("Paid €{amount} at {merchant} Spent"),
            expenditureMatcher = "AliExpress",
            expenditureName = "Other",
            currency = CURRENCY
        )
        val MESSAGE = Message(
            id = MESSAGE_ID,
            sender= "",
            text = "",
            status = Message.Rejected
        )
    }
}

object DummyTaskService : TaskService {

    override fun backgroundTask(wrapper: suspend (context: CoroutineScope) -> Unit) {
        runTest {
            wrapper(this)
        }
    }

    override fun blockingTask(wrapper: suspend (context: CoroutineScope) -> Unit) {
        runTest {
            wrapper(this)
        }
    }
}