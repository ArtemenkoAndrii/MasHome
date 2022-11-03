package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.Transformations
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SpendingListViewModel @AssistedInject constructor(
    private val spendingRepository: SpendingRepository,
    private val budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted budgetId: Int
): BaseListViewModel<Spending>(coroutineService) {
    val budget = budgetService.getBudgetLive(budgetId)
    val spendings = Transformations.map(budget) {
        spendingRepository.getByBudgetId(it.id)
    }

    override fun getRepository() = spendingRepository

    override suspend fun afterRemove(item: Spending) {
        budgetService.calculateBudget(item.expenditure.budget_id)
    }

    @AssistedFactory
    interface Factory {
        fun create(budgetId: Int): SpendingListViewModel
    }
}
