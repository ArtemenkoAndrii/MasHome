package com.mas.mobile.service

import com.mas.mobile.model.DayOfMonth
import com.mas.mobile.model.Period
import com.mas.mobile.model.Settings
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.SettingsRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.ExpenditureData
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetServiceTest {
    private val mockBudgetRepository = mockk<BudgetRepository>(relaxed = true)
    private val mockExpenditureRepository = mockk<ExpenditureRepository>(relaxed = true)
    private val mockResourceService = mockk<ResourceService>(relaxed = true)
    private val mockSettingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val mockSettings = mockk<Settings>(relaxed = true)

    private lateinit var testInstance: BudgetService

    private val insertedBudgetSlot = slot<Budget>()
    private val insertedExpenditureSlot = slot<Expenditure>()
    private val insertedExpenditureListSlot = mutableListOf<Expenditure>()
    private val dateSlot = slot<LocalDate>()

    @Before
    fun setUp() {
        every { mockSettingsRepository.get() } returns mockSettings
        testInstance = BudgetService(mockSettingsRepository, mockBudgetRepository, mockExpenditureRepository, mockResourceService)

        every { mockBudgetRepository.getActive() } returnsMany listOf(null, Budget(id = NEW_BUDGET_ID.toInt()))
        every { mockBudgetRepository.getLastCompletedOn(any()) } returns null
        coEvery { mockBudgetRepository.insert(capture(insertedBudgetSlot)) } returns NEW_BUDGET_ID

        every { mockExpenditureRepository.getByBudgetId(TEMPLATE_ID) } returns TEMPLATE_EXPENDITURES
        every { mockExpenditureRepository.clone(capture(insertedExpenditureSlot)) } answers { insertedExpenditureSlot.captured }
        coEvery { mockExpenditureRepository.insert(capture(insertedExpenditureListSlot)) } returns NEW_EXPENDITURE_ID

        every { mockResourceService.budgetTruncatedComment(capture(dateSlot)) } answers { String.format(COMMENT, dateSlot.captured) }

        every { mockSettings.period } returns Period.MONTH
        every { mockSettings.startDayOfMonth } returns DayOfMonth(5)
        every { mockSettings.startDayOfWeek } returns DayOfWeek.MONDAY
    }

    @Test
    fun `should create month budget`() {
        sequenceOf(
            MonthCase(DayOfMonth(7), "September 2022","07.09.2022",  "06.10.2022", ""),
            MonthCase(DayOfMonth(5), "September 2022","07.09.2022",  "04.10.2022", "The start date was automatically truncated. Original value is 2022-09-05"),
            MonthCase(DayOfMonth(15),"August 2022",   "10.09.2022",  "14.09.2022", "The start date was automatically truncated. Original value is 2022-08-15"),
            MonthCase(DayOfMonth(5), "December 2022", "15.12.2022",  "04.01.2023", "The start date was automatically truncated. Original value is 2022-12-05"),
            MonthCase(DayOfMonth(1), "January 2023",  "01.01.2023",  "31.01.2023", ""),
            MonthCase(DayOfMonth(1), "February 2023",  "01.02.2023",  "28.02.2023", ""),
        ).forEachIndexed { index, case ->
            every { mockSettings.period } returns Period.MONTH
            every { mockSettings.startDayOfMonth } returns case.dayOfMonth()
            createBudgetAndValidate(index,case)
        }
    }

    @Test
    fun `should create week budget`() {
        sequenceOf(
            WeekCase(DayOfWeek.MONDAY, "Monday 05/09/2022","05.09.2022",  "11.09.2022", ""),
            WeekCase(DayOfWeek.MONDAY, "Monday 05/09/2022","07.09.2022",  "11.09.2022", "The start date was automatically truncated. Original value is 2022-09-05"),
            WeekCase(DayOfWeek.FRIDAY, "Friday 30/12/2022","31.12.2022",  "05.01.2023", "The start date was automatically truncated. Original value is 2022-12-30"),
        ).forEachIndexed { index, case ->
            every { mockSettings.period } returns Period.WEEK
            every { mockSettings.startDayOfWeek } returns case.dayOfWeek()
            createBudgetAndValidate(index,case)
        }
    }

    @Test
    fun `should create two week budget`() {
        sequenceOf(
            WeekCase(DayOfWeek.MONDAY, "Monday 05/09/2022","05.09.2022",  "18.09.2022", ""),
            WeekCase(DayOfWeek.MONDAY, "Monday 05/09/2022","07.09.2022",  "18.09.2022", "The start date was automatically truncated. Original value is 2022-09-05"),
            WeekCase(DayOfWeek.FRIDAY, "Friday 30/12/2022","31.12.2022",  "12.01.2023", "The start date was automatically truncated. Original value is 2022-12-30"),
        ).forEachIndexed { index, case ->
            every { mockSettings.period } returns Period.TWO_WEEKS
            every { mockSettings.startDayOfWeek } returns case.dayOfWeek()
            createBudgetAndValidate(index,case)
        }
    }

    @Test
    fun `should create quarter budget`() {
        sequenceOf(
            Case("Q1 2022","01.01.2022",  "31.03.2022", ""),
            Case("Q3 2022","05.09.2022",  "30.09.2022", "The start date was automatically truncated. Original value is 2022-07-01"),
        ).forEachIndexed { index, case ->
            every { mockSettings.period } returns Period.QUARTER
            createBudgetAndValidate(index,case)
        }
    }

    @Test
    fun `should create year budget`() {
        sequenceOf(
            Case("2022","01.01.2022",  "31.12.2022", ""),
            Case("2022","05.09.2022",  "31.12.2022", "The start date was automatically truncated. Original value is 2022-01-01"),
        ).forEachIndexed { index, case ->
            every { mockSettings.period } returns Period.YEAR
            createBudgetAndValidate(index,case)
        }
    }

    @Test
    fun `should create active`() {
        testInstance.getActiveOrCreate()
        assertEquals(LocalDate.now(), insertedBudgetSlot.captured.startsOn)
    }

    private fun createBudgetAndValidate(index: Int, case: Case) {
        val idx = (index + 1).toString()

        testInstance.createNewBudget(case.actualStartDate())
        val newBudget = insertedBudgetSlot.captured

        // validate budget
        assertEquals(idx, GENERATE_ID, newBudget.id)
        assertEquals(idx, case.actualStartDate(), newBudget.startsOn)
        assertEquals(idx,case.estimatedEndDate(), newBudget.lastDayAt)
        assertEquals(idx,case.budgetName(), newBudget.name)
        assertEquals(idx,case.budgetComment(), newBudget.comment)
        assertEquals(idx,0.0, newBudget.plan, 0.0001)
        assertEquals(idx,0.0, newBudget.fact, 0.0001)

        // validate expenditures
        assertEquals(idx, TEMPLATE_EXPENDITURE_1.name, insertedExpenditureListSlot[0].name )
        assertEquals(idx, GENERATE_ID, insertedExpenditureListSlot[0].id )
        assertEquals(idx, TEMPLATE_EXPENDITURE_2.name, insertedExpenditureListSlot[1].name )
        assertEquals(idx, GENERATE_ID, insertedExpenditureListSlot[1].id )
    }

    open class Case(
        private val budgetName: String,
        private val actualStartDate: String,
        private val estimatedEndDate: String,
        private val budgetComment: String,
    ) {
        fun actualStartDate() = actualStartDate.toDate()
        fun estimatedEndDate() = estimatedEndDate.toDate()
        fun budgetName() = budgetName
        fun budgetComment() = budgetComment

        private fun String.toDate(): LocalDate =
            LocalDate.parse(this, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    class MonthCase(
        private val dayOfMonth: DayOfMonth,
        budgetName: String, actualStartDate: String, estimatedEndDate: String, budgetComment: String,
    ) : Case(budgetName, actualStartDate, estimatedEndDate, budgetComment) {
        fun dayOfMonth() = dayOfMonth
    }

    class WeekCase(
        private val dayOfWeek: DayOfWeek,
        budgetName: String, actualStartDate: String, estimatedEndDate: String, budgetComment: String,
    ) : Case(budgetName, actualStartDate, estimatedEndDate, budgetComment) {
        fun dayOfWeek() = dayOfWeek
    }

    private companion object {
        const val GENERATE_ID = 0
        const val NEW_BUDGET_ID = 1L
        const val NEW_EXPENDITURE_ID = 1L
        const val TEMPLATE_ID = 1
        val TEMPLATE_EXPENDITURE_1 = Expenditure(ExpenditureData(name = "exp1"), Budget())
        val TEMPLATE_EXPENDITURE_2 = Expenditure(ExpenditureData(name = "exp2"), Budget())
        val TEMPLATE_EXPENDITURES = listOf(TEMPLATE_EXPENDITURE_1, TEMPLATE_EXPENDITURE_2)
        const val COMMENT = "The start date was automatically truncated. Original value is %s"
    }
}