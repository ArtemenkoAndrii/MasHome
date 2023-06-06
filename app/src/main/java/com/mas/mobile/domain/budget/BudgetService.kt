package com.mas.mobile.domain.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mas.mobile.domain.settings.Period
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.service.ResourceService
import com.mas.mobile.util.DateTool
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetService @Inject constructor(
    private val resourceService: ResourceService,
    private val settingsRepository: SettingsRepository,
    val budgetRepository: BudgetRepository,
    val expenditureRepository: ExpenditureRepository,
    val spendingRepository: SpendingRepository
) {
    private val settings
        get() = settingsRepository.get()

    private var activeSource: LiveData<Budget> = MutableLiveData()
    private val activeLive = MediatorLiveData<Budget>()

    fun reloadActiveBudget() {
        activeLive.removeSource(activeSource)
        activeSource = budgetRepository.live.getBudget(getActiveBudget().id)

        activeLive.addSource(activeSource) {
            if (it == null) {
                reloadActiveBudget()
            } else {
                activeLive.postValue(it)
            }
        }
    }

    fun loadBudgetOrGetActive(budgetId: BudgetId): Budget =
        if (budgetId.value > 0) {
            budgetRepository.getBudget(budgetId.value)
        } else {
            getActiveBudget()
        }
        ?: throw BudgetException("Budget ${budgetId.value} not found.")

    fun loadLiveBudget(budgetId: BudgetId): LiveData<Budget> =
        if (budgetId.value > 0) {
            budgetRepository.live.getBudget(budgetId)
        } else {
            this.activeLive
        }

    fun getActiveBudget() =
        budgetRepository.getOnDate(LocalDate.now()) ?: createNext()

    fun isPeriodChangeable(budget: Budget) =
        budgetRepository.getLast()?.id == budget.id

    init {
        reloadActiveBudget()
    }

    fun createNext() = runBlocking {
        val startDate = budgetRepository.getLast()?.lastDayAt?.plusDays(1)?.let {
            maxOf(it, LocalDate.now())
        } ?: LocalDate.now()

        val budget = createBudget(startDate).also {
            uniquifyName(it)
            populateExpenditures(it)
        }

        budgetRepository.save(budget)

        return@runBlocking budget
    }

    fun recreateActiveBudget() = runBlocking {
        budgetRepository.remove(getActiveBudget())
    }

    suspend fun spend(date: LocalDateTime, amount: Double, comment: String, expenditureName: String): SpendingId {
        val budget = getActiveBudget()
        val expenditure = budget.budgetDetails.expenditure
            .firstOrNull { it.name.lowercase() == expenditureName.trimIndent().lowercase() }
            ?: expenditureRepository.create().also {
                it.name = expenditureName.trim()
            }

        val spending = spendingRepository.create().also {
            it.comment = comment.trim()
            it.date = date
            it.amount = amount
            it.expenditure = expenditure
        }

        budget.addSpending(spending)
        budgetRepository.save(budget)

        return spending.id
    }

    private fun populateExpenditures(budget: Budget) {
        budgetRepository.getBudget(TEMPLATE_BUDGET_ID)?.budgetDetails?.expenditure?.forEach { sample ->
            val expenditure = expenditureRepository.create().also {
                it.name = sample.name
                it.plan = sample.plan
            }

            budget.addExpenditure(expenditure)
        }
    }

    private fun uniquifyName(budget: Budget) {
        var name = budget.name
        var i = DEFAULT_NAME_SUFFIX
        while (budgetRepository.getBudgetByName(name) != null) {
            name = budget.name + "($i)"
            i++
        }

        budget.name = name
    }

    private fun createBudget(startDate: LocalDate): Budget {
        val budget = budgetRepository.createBudget()
        when (settings.period) {
            Period.MONTH -> prepareMonthBudget(budget, startDate)
            Period.WEEK -> prepareWeekBudget(budget, startDate)
            Period.TWO_WEEKS -> prepareWeekBudget(budget, startDate).also {
                budget.lastDayAt = budget.lastDayAt.plusDays(7)
            }
            Period.QUARTER -> prepareQuarterBudget(budget, startDate)
            Period.YEAR -> prepareYearBudget(budget, startDate)
        }
        return budget
    }

    private fun prepareWeekBudget(budget: Budget, actualStartDate: LocalDate) {
        val periodStartDay = settings.startDayOfWeek
        val diff = if (actualStartDate.dayOfWeek.value >= periodStartDay.value) {
            actualStartDate.dayOfWeek.value - periodStartDay.value
        } else {
            7 - periodStartDay.value + actualStartDate.dayOfWeek.value
        }
        val estimatedStartDate = actualStartDate.minusDays(diff.toLong())
        val estimatedEndDate = estimatedStartDate.plusDays(6)

        budget.name = DateTool.formatToWeekWithDate(estimatedStartDate)
        budget.startsOn = actualStartDate
        budget.lastDayAt = estimatedEndDate
        budget.comment = generateComment(estimatedStartDate, actualStartDate)
    }

    private fun prepareMonthBudget(budget: Budget, actualStartDate: LocalDate) {
        val periodStartDay = settings.startDayOfMonth.value
        val estimatedStartDate = if (actualStartDate.dayOfMonth >= periodStartDay) {
            actualStartDate.withDayOfMonth(periodStartDay)
        } else {
            actualStartDate.minusMonths(1).withDayOfMonth(periodStartDay)
        }
        val estimatedEndDate = estimatedStartDate.plusMonths(1).minusDays(1)

        budget.name = DateTool.formatToMonthWithYear(estimatedStartDate)
        budget.startsOn = actualStartDate
        budget.lastDayAt = estimatedEndDate
        budget.comment = generateComment(estimatedStartDate, actualStartDate)
    }

    private fun prepareQuarterBudget(budget: Budget, actualStartDate: LocalDate) {
        val quarterlyMonth = listOf(10, 7, 4, 1).first{ actualStartDate.month.value >= it }
        val estimatedStartDate = actualStartDate.withMonth(quarterlyMonth).withDayOfMonth(1)
        val estimatedEndDate = estimatedStartDate.plusMonths(2).let { it.withDayOfMonth(it.month.length(it.isLeapYear)) }

        budget.name = DateTool.formatToQuarterWithYear(estimatedStartDate)
        budget.startsOn = actualStartDate
        budget.lastDayAt = estimatedEndDate
        budget.comment = generateComment(estimatedStartDate, actualStartDate)
    }

    private fun prepareYearBudget(budget: Budget, actualStartDate: LocalDate) {
        val estimatedStartDate = actualStartDate.withMonth(1).withDayOfMonth(1)
        val estimatedEndDate = estimatedStartDate.withMonth(12).let { it.withDayOfMonth(it.month.length(it.isLeapYear)) }

        budget.name = DateTool.formatToYear(estimatedStartDate)
        budget.startsOn = actualStartDate
        budget.lastDayAt = estimatedEndDate
        budget.comment = generateComment(estimatedStartDate, actualStartDate)
    }

    private fun generateComment(estimatedStartDate: LocalDate, actualStartDate: LocalDate) =
        if (estimatedStartDate < actualStartDate) {
            resourceService.budgetTruncatedComment(estimatedStartDate)
        } else {
            ""
        }

    companion object {
        const val TEMPLATE_BUDGET_ID = 1
        const val DEFAULT_NAME_SUFFIX = 2
    }
}