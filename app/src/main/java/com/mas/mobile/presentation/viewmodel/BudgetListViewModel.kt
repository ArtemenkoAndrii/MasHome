package com.mas.mobile.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mas.mobile.repository.BudgetRepository
import com.mas.mobile.repository.db.entity.Budget
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BudgetListViewModel(application: Application): CommonListViewModel<Budget>(application) {
    val budgets: LiveData<List<Budget>>

    init {
        BudgetRepository.initDb(application)
        budgets = BudgetRepository.live.getAll()
    }

    fun remove(item: Budget) {
        doRemove(item) {
            BudgetRepository.delete(item)
        }
    }

}