package com.mas.mobile.service

import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.db.entity.Expenditure
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenditureService @Inject constructor(
    private val budgetService: BudgetService,
    private val expenditureRepository: ExpenditureRepository
) {
    fun findOrCreate(name: String): Expenditure {
        val nameForSearch = name.trim().uppercase()
        val activeBudgetId = budgetService.getActiveOrCreate().id
        val expenditure = expenditureRepository.getByBudgetId(activeBudgetId)
            .firstOrNull { it.name.uppercase() == nameForSearch }
        return expenditure ?: createExpenditure(nameForSearch, activeBudgetId)
    }

    private fun createExpenditure(name: String, budgetId: Int): Expenditure {
        val newExpenditure = expenditureRepository.createNew().also {
            it.data.name = name
            it.data.budget_id = budgetId
        }

        val id = runBlocking {
            expenditureRepository.insert(newExpenditure)
        }

        return expenditureRepository.getById(id.toInt())!!
    }

}