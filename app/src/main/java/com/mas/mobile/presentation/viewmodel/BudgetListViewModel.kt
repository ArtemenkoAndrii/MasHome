package com.mas.mobile.presentation.viewmodel

import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.service.BudgetService
import com.mas.mobile.service.CoroutineService
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate

class BudgetListViewModel @AssistedInject constructor(
    private val budgetRepository: BudgetRepository,
    private val budgetService: BudgetService,
    private val coroutineService: CoroutineService,
): BaseListViewModel<Budget>(coroutineService) {
    val budgets = budgetRepository.live.getAll()

    override fun getRepository() = budgetRepository

    fun createBudget() = budgetService.createNext()

    fun isChangeable(item: Budget) =
        budgetRepository.getLastCompletedOn(LocalDate.MAX)?.let {
            it.id == item.id
        } ?: true

    override fun remove(item: Budget) {
        coroutineService.backgroundTask {
            getRepository().delete(item)
            // Just for case if the only budget will be removed
            budgetService.reloadBudget()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): BudgetListViewModel
    }
}
