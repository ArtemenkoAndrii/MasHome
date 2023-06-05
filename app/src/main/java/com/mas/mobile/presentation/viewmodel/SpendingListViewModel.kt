package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.domain.Repository
import com.mas.mobile.domain.budget.*
import com.mas.mobile.service.CoroutineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate

class SpendingListViewModel @AssistedInject constructor(
    spendingRepository: SpendingRepository,
    budgetService: BudgetService,
    coroutineService: CoroutineService,
    @Assisted private val budgetId: Int
): ListViewModel<Spending>(coroutineService, SpendingListRepositoryAdapter(budgetService)) {
    val budget = budgetService.loadLiveBudget(BudgetId(budgetId))
    val spendings =
        if (budgetId > 0) {
            spendingRepository.getLiveSpendings(BudgetId(budgetId))
        } else {
            spendingRepository.getLiveSpendings(LocalDate.now().minusDays(30))
        }

    @AssistedFactory
    interface Factory {
        fun create(budgetId: Int): SpendingListViewModel
    }
}

private class SpendingListRepositoryAdapter(val service: BudgetService) : Repository<Spending> {
    override suspend fun save(item: Spending) {
        with(item) {
            it.addSpending(item)
        }
    }

    override suspend fun remove(item: Spending) {
        with(item) {
            it.removeSpending(item)
        }
    }

    private suspend fun with(item: Spending, perform: (budget: Budget) -> Unit) {
        val budget = service.loadBudget(item.expenditure.budgetId)
        perform(budget)
        service.budgetRepository.save(budget)
    }
}
