package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.R
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.SettingsService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ExpenditureListViewModel @AssistedInject constructor(
    private val expenditureRepository: ExpenditureRepository,
    private val settingsService: SettingsService,
    coroutineService: CoroutineService,
    budgetService: BudgetService,
    @Assisted budgetId: Int
) : BaseListViewModel<Expenditure>(coroutineService) {
    private val isFirstLaunchSession = settingsService.isThisFirstLaunch().also {
        if (it) { settingsService.commitFirstRun() }
    }
    private var isFirstLaunchInfo = isFirstLaunchSession

    val budget = budgetService.getBudget(budgetId)
    val expenditures: LiveData<List<Expenditure>> = Transformations.map(budget) {
            expenditureRepository.getByBudgetId(it.id)
        }
    val color = Transformations.map(budget) { calcColor(it) }

    override fun getRepository() = expenditureRepository

    fun isFirstLaunchSession() = isFirstLaunchSession
    fun isFirstLaunchInfo() = isFirstLaunchInfo.also { isFirstLaunchInfo = false }

    private fun calcColor(budget: Budget) =
        when {
            budget.getProgress() > 150 -> R.color.colorRed
            budget.getProgress() > 100 -> R.color.colorYellow
            else -> R.color.colorAccent
        }

    @AssistedFactory
    interface Factory {
        fun create(budgetId: Int): ExpenditureListViewModel
    }
}
