package com.mas.mobile.domain.budget

import com.mas.mobile.domain.settings.DayOfMonth
import com.mas.mobile.domain.settings.Period
import com.mas.mobile.domain.settings.Settings
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.service.ErrorHandler
import com.mas.mobile.service.ResourceService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.stream.Stream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BudgetServiceTest {
    private val mockBudgetRepository = mockk<BudgetRepository>(relaxed = true)
    private val mockExpenditureRepository = mockk<ExpenditureRepository>(relaxed = true)
    private val mockResourceService = mockk<ResourceService>(relaxed = true)
    private val mockSettingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val mockSettings = mockk<Settings>(relaxed = true)
    private val mockCategoryRepository = mockk<CategoryRepository>(relaxed = true)

    private var testInstance = BudgetService(
        mockResourceService,
        mockSettingsRepository,
        mockk<SpendingRepository>(relaxed = true),
        mockk<ExchangeRepository>(relaxed = true),
        mockCategoryRepository,
        mockk<ErrorHandler>(relaxed = true),
        mockBudgetRepository,
        mockExpenditureRepository
    )

    private val dateSlot = slot<LocalDate>()

    @BeforeEach
    fun setUp() {
        every { mockSettingsRepository.get() } returns mockSettings
        every { mockSettings.currency } returns Currency.getInstance("EUR")

        every { mockBudgetRepository.createBudget() } returns Budget(id = BudgetId(BUDGET_ID), lazyLoader = lazy { BudgetDetails( mutableListOf(), mutableListOf()) })
        every { mockBudgetRepository.getBudget(any()) } returns TEMPLATE
        every { mockBudgetRepository.getBudgetByName(any()) } returns null

        every { mockExpenditureRepository.create() } answers { Expenditure(ExpenditureId(getNextInt()), "", CategoryId(-1), 0.0, 0.0, "", BudgetId(BUDGET_ID)) }

        every { mockResourceService.budgetTruncatedComment(capture(dateSlot)) } answers { String.format(COMMENT, dateSlot.captured) }
        every { mockCategoryRepository.getAll() } returns CATEGORIES
    }

    private fun periodCases() = Stream.of(
        Case(Period.MONTH, DayOfMonth(7), "September 2022","07.09.2022",  "06.10.2022", ""),
        Case(Period.MONTH, DayOfMonth(5), "September 2022","07.09.2022",  "04.10.2022", "The start date was automatically truncated. Original value is 2022-09-05"),
        Case(Period.MONTH, DayOfMonth(15),"August 2022",   "10.09.2022",  "14.09.2022", "The start date was automatically truncated. Original value is 2022-08-15"),
        Case(Period.MONTH, DayOfMonth(5), "December 2022", "15.12.2022",  "04.01.2023", "The start date was automatically truncated. Original value is 2022-12-05"),
        Case(Period.MONTH, DayOfMonth(1), "January 2023",  "01.01.2023",  "31.01.2023", ""),
        Case(Period.MONTH, DayOfMonth(1), "February 2023",  "01.02.2023",  "28.02.2023", ""),

        Case(Period.WEEK, DayOfWeek.MONDAY, "Monday 05/09/2022","05.09.2022",  "11.09.2022", ""),
        Case(Period.WEEK, DayOfWeek.MONDAY,"Monday 05/09/2022","07.09.2022",  "11.09.2022", "The start date was automatically truncated. Original value is 2022-09-05"),
        Case(Period.WEEK, DayOfWeek.FRIDAY,"Friday 30/12/2022","31.12.2022",  "05.01.2023", "The start date was automatically truncated. Original value is 2022-12-30"),

        Case(Period.TWO_WEEKS, DayOfWeek.MONDAY, "Monday 05/09/2022","05.09.2022",  "18.09.2022", ""),
        Case(Period.TWO_WEEKS, DayOfWeek.MONDAY, "Monday 05/09/2022","07.09.2022",  "18.09.2022", "The start date was automatically truncated. Original value is 2022-09-05"),
        Case(Period.TWO_WEEKS, DayOfWeek.FRIDAY, "Friday 30/12/2022","31.12.2022",  "12.01.2023", "The start date was automatically truncated. Original value is 2022-12-30"),

        Case(Period.QUARTER, null, "Q1 2022","01.01.2022",  "31.03.2022", ""),
        Case(Period.QUARTER, null, "Q3 2022","05.09.2022",  "30.09.2022", "The start date was automatically truncated. Original value is 2022-07-01"),

        Case(Period.YEAR,null, "2022","01.01.2022",  "31.12.2022", ""),
        Case(Period.YEAR,null, "2022","05.09.2022",  "31.12.2022", "The start date was automatically truncated. Original value is 2022-01-01")
    )

    @ParameterizedTest
    @MethodSource("periodCases")
    fun `test budget periods` (case: Case) {
        every { mockSettings.period } returns case.period
        when (case.now) {
            is DayOfMonth -> {
                every { mockSettings.startDayOfMonth } returns case.now
            }
            is DayOfWeek -> {
                every { mockSettings.startDayOfWeek } returns case.now
            }
        }

        val newBudget = testInstance.createBudget(case.actualStartDate.toDate())

        assertEquals(BudgetId(BUDGET_ID), newBudget.id)
        assertEquals(case.actualStartDate.toDate(), newBudget.startsOn)
        assertEquals(case.estimatedEndDate.toDate(), newBudget.lastDayAt)
        assertEquals(case.budgetName, newBudget.name)
        assertEquals(case.budgetComment, newBudget.comment)
        assertEquals(ZERO, newBudget.fact, 0.0001)
    }

    @Test
    fun `test template copying` () {
        with (testInstance.createNext()) {
            assertEquals(CURRENCY, currency)

            assertEquals(budgetDetails.expenditure.size, 2)

            assertEquals(CATEGORIES[0].name, budgetDetails.expenditure[0].name)
            assertEquals(CATEGORIES[0].plan, budgetDetails.expenditure[0].plan)

            assertEquals(CATEGORIES[1].name, budgetDetails.expenditure[1].name)
            assertEquals(CATEGORIES[1].plan, budgetDetails.expenditure[1].plan)
        }
    }

    data class Case(
        val period: Period,
        val now: Any?,
        val budgetName: String,
        val actualStartDate: String,
        val estimatedEndDate: String,
        val budgetComment: String
    )

    private fun String.toDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    private companion object {
        const val ZERO = 0.0
        const val TEMPLATE_ID = 1
        const val BUDGET_ID = 0
        const val COMMENT = "The start date was automatically truncated. Original value is %s"

        val CURRENCY = Currency.getInstance("EUR")
        val TEMPLATE = Budget(
            id = BudgetId(TEMPLATE_ID),
            currency = CURRENCY,
            lazyLoader = lazy {
                BudgetDetails(
                    expenditure = mutableListOf(),
                    spending = mutableListOf()
                )
            }
        )

        val CATEGORIES = mutableListOf(
            Category(
                id = CategoryId(1),
                name = "Category1",
                plan = 1.0,
                description = "Description 1",
                isActive = true,
                merchants = emptyList()
            ),
            Category(
                id = CategoryId(2),
                name = "Category2",
                plan = 2.0,
                description = "Description 2",
                isActive = true,
                merchants = emptyList()
            ),
            Category(
                id = CategoryId(3),
                name = "Category3",
                plan = 3.0,
                description = "Description 3",
                isActive = false,
                merchants = emptyList()
            )
        )

        var getNextInt = 10
        fun getNextInt() = ++getNextInt
    }
}