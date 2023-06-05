package com.mas.mobile.domain.budget

import androidx.lifecycle.LiveData
import com.mas.mobile.domain.Repository
import java.time.LocalDate

interface BudgetRepository : Repository<Budget> {
    val live: BudgetLiveData

    fun createBudget(): Budget
    fun getBudget(budgetId: Int): Budget?
    fun getBudgetByName(name: String): Budget?
    fun getBudgetByExpenditureId(id: ExpenditureId): Budget?
    fun getBudgetBySpendingId(id: SpendingId): Budget?
    fun getOnDate(date: LocalDate): Budget?
    fun getLast(): Budget?

    override suspend fun save(item: Budget)
    override suspend fun remove(item: Budget)

    fun generateId(): Long
}

interface BudgetLiveData {
    fun getBudget(budgetId: BudgetId): LiveData<Budget>
    fun getBudgets(): LiveData<List<Budget>>
//    fun getExpenditures(budgetId: BudgetId): LiveData<List<Expenditure>>
//    fun getSpendings(budgetId: BudgetId): LiveData<List<Spending>>
//    fun getSpendings(expenditureId: ExpenditureId): LiveData<List<Spending>>
}
