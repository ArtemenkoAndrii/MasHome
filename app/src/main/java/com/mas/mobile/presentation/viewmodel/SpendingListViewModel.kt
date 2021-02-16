package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.LiveData
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SpendingListViewModel @AssistedInject constructor(
    private val spendingRepository: SpendingRepository,
    budgetRepository: BudgetRepository,
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted budgetId: Int
): BaseListViewModel<Spending>(coroutineService) {
    val spendings: LiveData<List<Spending>>
    val budgetName: String

    init {
        spendings = if (budgetId == ACTIVE_BUDGETS) {
            budgetService.getActiveOrCreate().run {
                spendingRepository.live.getByBudgetId(this.id)
            }
        } else {
            spendingRepository.live.getByBudgetId(budgetId)
        }

        budgetName = budgetRepository.getById(budgetId)?.name ?: ""
    }

    override fun getRepository() = spendingRepository

    @AssistedFactory
    interface Factory {
        fun create(budgetId: Int): SpendingListViewModel
    }

    private companion object {
        const val ACTIVE_BUDGETS = -1
    }
}
