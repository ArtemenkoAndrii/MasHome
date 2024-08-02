package com.mas.mobile.domain.budget

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency
import kotlin.test.assertEquals

class BudgetTest {

    private lateinit var testInstance: Budget

    @BeforeEach
    fun setUp() {
        testInstance = getEmptyBudget()
    }

    @Test
    fun `should add expenditure` () {
        testInstance.addExpenditure(FOOD_EXPENDITURE)

        assertEquals(FOOD_EXPENDITURE, testInstance.budgetDetails.expenditure[0])
        assertEquals(FOOD_EXPENDITURE, testInstance.getExpenditure(ExpenditureId(2)))
        assertEquals(100.00, testInstance.plan)
        assertEquals(0.00, testInstance.fact)
        assertEquals(0, testInstance.getProgress())
        assertEquals("0.00 / 100.00", testInstance.getStatus())
    }

    @Test
    fun `should update expenditure` () {
        `should add expenditure`()

        testInstance.addExpenditure(FOOD_EXPENDITURE.copy(plan = 300.00))

        assertEquals(FOOD_EXPENDITURE.id, testInstance.budgetDetails.expenditure[0].id)
        assertEquals(1, testInstance.budgetDetails.expenditure.size)
        assertEquals(300.00, testInstance.plan)
    }

    @Test
    fun `should add spending` () {
        `should add expenditure`()

        testInstance.addSpending(FOOD_SPENDING)

        assertEquals(FOOD_SPENDING, testInstance.budgetDetails.spending[0])
        assertEquals(1, testInstance.budgetDetails.expenditure.size)
        assertEquals(100.00, testInstance.plan)
        assertEquals(20.00, testInstance.fact)
        assertEquals(20, testInstance.getProgress())
        assertEquals("20.00 / 100.00", testInstance.getStatus())
    }

    @Test
    fun `should update spending` () {
        `should add spending`()

        testInstance.addSpending(FOOD_SPENDING.copy(amount = 15.00))

        assertEquals(FOOD_SPENDING.id, testInstance.budgetDetails.spending[0].id)
        assertEquals(1, testInstance.budgetDetails.spending.size)
        assertEquals(15.00, testInstance.fact)
    }

    @Test
    fun `should remove spending` () {
        `should add spending` ()

        testInstance.addSpending(FOOD_SPENDING_2)
        assertEquals(FOOD_SPENDING, testInstance.budgetDetails.spending[0])
        assertEquals(1, testInstance.budgetDetails.expenditure.size)
        assertEquals(100.00, testInstance.plan)
        assertEquals(25.00, testInstance.fact)
        assertEquals(25, testInstance.getProgress())
        assertEquals("25.00 / 100.00", testInstance.getStatus())

        testInstance.removeSpending(FOOD_SPENDING_2)
        assertEquals(FOOD_SPENDING, testInstance.budgetDetails.spending[0])
        assertEquals(1, testInstance.budgetDetails.expenditure.size)
        assertEquals(100.00, testInstance.plan)
        assertEquals(20.00, testInstance.fact)
        assertEquals(20, testInstance.getProgress())
        assertEquals("20.00 / 100.00", testInstance.getStatus())
    }

    @Test
    fun `should add expenditure implicitly` () {
        `should add spending` ()

        testInstance.addSpending(PETROL_SPENDING)
        assertEquals(2, testInstance.budgetDetails.expenditure.size)
        assertEquals(FOOD_EXPENDITURE, testInstance.budgetDetails.expenditure[0])
        assertEquals(PETROL_EXPENDITURE, testInstance.budgetDetails.expenditure[1])
        assertEquals(250.00, testInstance.plan)
        assertEquals(100.00, testInstance.fact)
        assertEquals(40, testInstance.getProgress())
        assertEquals("100.00 / 250.00", testInstance.getStatus())
    }

    @Test
    fun `should remove expenditure` () {
        `should add spending`()

        testInstance.addExpenditure(PETROL_EXPENDITURE)
        assertEquals(2, testInstance.budgetDetails.expenditure.size)
        assertEquals(250.00, testInstance.plan)
        assertEquals(20.00, testInstance.fact)

        testInstance.removeExpenditure(PETROL_EXPENDITURE)
        assertEquals(1, testInstance.budgetDetails.expenditure.size)
        assertEquals(FOOD_EXPENDITURE, testInstance.budgetDetails.expenditure[0])
        assertEquals(100.00, testInstance.plan)
        assertEquals(20.00, testInstance.fact)
    }

    @Test
    fun `should find expenditure ignoring case` () {
        `should add spending` ()

        assertEquals(
            FOOD_EXPENDITURE,
            testInstance.findExpenditure("food")
        )
        assertNull(
            testInstance.findExpenditure("non-existing")
        )
    }

    companion object {
        val PETROL_EXPENDITURE = Expenditure(
            ExpenditureId(1),
            "Petrol",
            CategoryId(1),
            150.00,
            0.00,
            "A95",
            BudgetId(0)
        )

        val FOOD_EXPENDITURE = Expenditure(
            ExpenditureId(2),
            "Food",
            CategoryId(0),
            100.00,
            0.00,
            "",
            BudgetId(0)
        )

        val FOOD_SPENDING = Spending(
            SpendingId(1),
            "McDonalds",
            LocalDateTime.now(),
            20.00,
            FOOD_EXPENDITURE,
            null
        )

        val FOOD_SPENDING_2 = Spending(
            SpendingId(2),
            "Starbucks",
            LocalDateTime.now(),
            5.00,
            FOOD_EXPENDITURE,
            null
        )

        val PETROL_SPENDING = Spending(
            SpendingId(3),
            "Shell",
            LocalDateTime.now(),
            80.00,
            PETROL_EXPENDITURE,
            null
        )

        private fun getEmptyBudget(): Budget {
            return Budget(
                id = BudgetId(1),
                name = "August 2024",
                plan = 0.00,
                fact = 0.00,
                startsOn = LocalDate.of(2024, 8, 1),
                lastDayAt = LocalDate.of(2024, 8, 31),
                comment = "Comment",
                currency = Currency.getInstance("EUR"),
                lazyLoader = lazy {
                    BudgetDetails(
                        mutableListOf(),
                        mutableListOf()
                    )
                }
            )
        }
    }
}