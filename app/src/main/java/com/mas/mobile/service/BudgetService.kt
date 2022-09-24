package com.mas.mobile.service

import android.util.Log
import com.mas.mobile.model.Period
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.SettingsRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.util.DateTool
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject

class BudgetService @Inject constructor(
    settingsRepository: SettingsRepository,
    private val budgetRepository: BudgetRepository,
    private val expenditureRepository: ExpenditureRepository,
    private val resourceService: ResourceService
) {
    private val settings = settingsRepository.get()

    fun getActiveOrCreate(): Budget {
        val budget = budgetRepository.getActive()
        return if (budget != null) {
            budget
        } else {
            createNewBudget(LocalDate.now())
            budgetRepository.getActive()?: throw Exception().also {
                Log.e(this::class.simpleName, "Active budget was not generated.", it)
            }
        }
    }

    fun createNext() {
        budgetRepository.getLastCompletedOn(LocalDate.MAX)?.let {
            createNewBudget(it.lastDayAt.plusDays(1))
        }
    }

    fun createNewBudget(onDate: LocalDate) {
        val lastCompleted = budgetRepository.getLastCompletedOn(onDate)
        val startDate = if (lastCompleted == null) {
            onDate
        } else {
            lastCompleted.lastDayAt.plusDays(1)
        }
        val budget = createBudget(startDate)
        saveWithExpenditures(budget)
    }

    private fun saveWithExpenditures(budget: Budget) {
        runBlocking {
            val id = budgetRepository.insert(budget)
            if (id > 0) {
                expenditureRepository.getByBudgetId(TEMPLATE_BUDGET_ID).forEach { template ->
                    val exp = expenditureRepository.clone(template).also { it.data.budget_id = id.toInt() }
                    expenditureRepository.insert(exp)
                }
            }
            else {
                Log.w(this::class.simpleName, "Budget was inserted with non-positive id")
            }
        }
    }

    private fun createBudget(startDate: LocalDate) =
        when(settings.period) {
            Period.MONTH -> createMonthBudget(startDate)
            Period.WEEK -> createWeekBudget(startDate)
            Period.TWO_WEEKS -> createWeekBudget(startDate).also {
                it.lastDayAt = it.lastDayAt.plusDays(7)
            }
            Period.QUARTER -> createQuarterBudget(startDate)
            Period.YEAR -> createYearBudget(startDate)
        }

    private fun createWeekBudget(actualStartDate: LocalDate): Budget {
        val periodStartDay = settings.startDayOfWeek
        val diff = if (actualStartDate.dayOfWeek.value >= periodStartDay.value) {
            actualStartDate.dayOfWeek.value - periodStartDay.value
        } else {
            7 - periodStartDay.value + actualStartDate.dayOfWeek.value
        }
        val estimatedStartDate = actualStartDate.minusDays(diff.toLong())
        val estimatedEndDate = estimatedStartDate.plusDays(6)
        return Budget(
            name = DateTool.formatToWeekWithDate(estimatedStartDate),
            startsOn = actualStartDate,
            lastDayAt = estimatedEndDate,
            comment = generateComment(estimatedStartDate, actualStartDate)
        )
    }

    private fun createMonthBudget(actualStartDate: LocalDate): Budget {
        val periodStartDay = settings.startDayOfMonth.value
        val estimatedStartDate = if (actualStartDate.dayOfMonth >= periodStartDay) {
            actualStartDate.withDayOfMonth(periodStartDay)
        } else {
            actualStartDate.minusMonths(1).withDayOfMonth(periodStartDay)
        }
        val estimatedEndDate = estimatedStartDate.plusMonths(1).minusDays(1)
        return  Budget(
            name = DateTool.formatToMonthWithYear(estimatedStartDate),
            startsOn = actualStartDate,
            lastDayAt = estimatedEndDate,
            comment = generateComment(estimatedStartDate, actualStartDate)
        )
    }

    private fun createQuarterBudget(actualStartDate: LocalDate): Budget {
        val quarterlyMonth = listOf(10, 7, 4, 1).first{ actualStartDate.month.value >= it }
        val estimatedStartDate = actualStartDate.withMonth(quarterlyMonth).withDayOfMonth(1)
        val estimatedEndDate = estimatedStartDate.plusMonths(2).let { it.withDayOfMonth(it.month.length(it.isLeapYear)) }
        return Budget(
            name = DateTool.formatToQuarterWithYear(estimatedStartDate),
            startsOn = actualStartDate,
            lastDayAt = estimatedEndDate,
            comment = generateComment(estimatedStartDate, actualStartDate)
        )
    }

    private fun createYearBudget(actualStartDate: LocalDate): Budget {
        val estimatedStartDate = actualStartDate.withMonth(1).withDayOfMonth(1)
        val estimatedEndDate = estimatedStartDate.withMonth(12).let { it.withDayOfMonth(it.month.length(it.isLeapYear)) }
        return Budget(
            name = DateTool.formatToYear(estimatedStartDate),
            startsOn = actualStartDate,
            lastDayAt = estimatedEndDate,
            comment = generateComment(estimatedStartDate, actualStartDate)
        )
    }

    private fun generateComment(estimatedStartDate: LocalDate, actualStartDate: LocalDate) =
        if (estimatedStartDate < actualStartDate) {
            resourceService.budgetTruncatedComment(estimatedStartDate)
        } else {
            ""
        }

    companion object {
        const val TEMPLATE_BUDGET_ID = 1
    }
}