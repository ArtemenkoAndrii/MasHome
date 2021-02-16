package com.mas.mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.Expenditure

class ExpenditureListViewModel(application: Application, budgetId: Int?): CommonListViewModel<Expenditure>(application) {
    val expenditures: LiveData<List<Expenditure>>

    init {
        ExpenditureRepository.initDb(application)

        val id = if (budgetId ?: 0 > 0) {
            budgetId!!
        } else {
            BudgetRepository.initDb(application)
            val activeBudget: Budget = BudgetRepository.getActive()
            activeBudget.id
        }

        expenditures = ExpenditureRepository.live.getByBudgetId(id)
    }

    fun remove(item: Expenditure) {
        doRemove(item) {
            ExpenditureRepository.delete(item)
        }
    }

}