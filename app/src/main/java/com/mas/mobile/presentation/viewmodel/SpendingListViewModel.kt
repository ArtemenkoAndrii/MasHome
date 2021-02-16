package com.mas.mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.Spending

class SpendingListViewModel(application: Application, budgetId: Int?): CommonListViewModel<Spending>(application) {
    val spendings: LiveData<List<Spending>>

    init {
        SpendingRepository.initDb(application)

        spendings = if (budgetId ?: 0 > 0) {
            SpendingRepository.live.getByBudgetId(budgetId!!)
        } else {
            BudgetRepository.initDb(application)
            val activeBudget: Budget = BudgetRepository.getActive()
            SpendingRepository.live.getByBudgetId(activeBudget.id)
        }
    }

    fun remove(item: Spending) {
        doRemove(item) {
            SpendingRepository.delete(item)
        }
    }
}