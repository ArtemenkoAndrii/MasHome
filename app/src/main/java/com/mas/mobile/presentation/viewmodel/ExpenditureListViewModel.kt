package com.mas.mobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.R
import com.mas.mobile.presentation.activity.converter.MoneyConverter
import com.mas.mobile.presentation.viewmodel.BudgetViewModel.Companion.TEMPLATE_BUDGET_ID
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.ExpenditureRepository
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
    context: Context,
    budgetRepository: BudgetRepository,
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted budgetId: Int
) : BaseListViewModel<Expenditure>(coroutineService) {
    private val isFirstLaunchSession = settingsService.isThisFirstLaunch().also {
        if (it) { settingsService.commitFirstRun() }
    }
    private var isFirstLaunchInfo = isFirstLaunchSession

    private val budget  = if (budgetId == ACTIVE_BUDGET) {
        budgetService.getActiveOrCreate()
    } else {
        budgetRepository.getById(budgetId)!!
    }
    private val liveBudget = budgetRepository.live.getById(budget.id)

    val expenditures: LiveData<List<Expenditure>> = expenditureRepository.live.getByBudgetId(budget.id)
    val budgetName: String
    val progress: LiveData<Int> = Transformations.map(liveBudget) { budget ->
        (budget.fact * 100 / budget.plan).toInt()
    }
    val status: LiveData<String> = Transformations.map(liveBudget) { budget ->
        "${MoneyConverter.doubleToString(budget.fact)} / ${MoneyConverter.doubleToString(budget.plan)}"
    }
    val color: LiveData<Int> = Transformations.map(progress) {
        when {
            it > 150 -> R.color.colorRed
            it > 100 -> R.color.colorYellow
            else -> R.color.colorAccent
        }
    }

    init {
        budgetName = if (budgetId == TEMPLATE_BUDGET_ID) {
            context.getString(R.string.title_budget_template)
        } else {
            budget.name
        }
    }

    override fun getRepository() = expenditureRepository

    fun isFirstLaunchSession() = isFirstLaunchSession
    fun isFirstLaunchInfo() = isFirstLaunchInfo.also { isFirstLaunchInfo = false }

    @AssistedFactory
    interface Factory {
        fun create(budgetId: Int): ExpenditureListViewModel
    }

    private companion object {
        const val ACTIVE_BUDGET = -1
    }
}
