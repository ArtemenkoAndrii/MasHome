package com.mas.mobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import com.mas.mobile.R
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

    val expenditures: LiveData<List<Expenditure>>
    val budgetName: String

    init {
        val id = if (budgetId == ACTIVE_BUDGET) {
            budgetService.getActiveOrCreate().id
        } else {
            budgetId
        }

        expenditures = expenditureRepository.live.getByBudgetId(id)
        budgetName = if (budgetId == TEMPLATE_BUDGET_ID) {
            context.getString(R.string.title_budget_template)
        } else {
            budgetRepository.getById(id)?.name ?: ""
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
