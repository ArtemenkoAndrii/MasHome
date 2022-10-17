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
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted budgetId: Int
): BaseListViewModel<Spending>(coroutineService) {
    val budget = budgetService.getBudget(budgetId)
    val spendings = Transformations.map(budget) {
        spendingRepository.getByBudgetId(it.id)
    }

    override fun getRepository() = spendingRepository

    @AssistedFactory
    interface Factory {
        fun create(budgetId: Int): SpendingListViewModel
    }
}
