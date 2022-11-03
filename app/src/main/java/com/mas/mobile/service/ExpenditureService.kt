package com.mas.mobile.service

import com.mas.mobile.repository.ExpenditureRepository
import com.mas.mobile.repository.SpendingRepository
import com.mas.mobile.repository.db.entity.Expenditure
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenditureService @Inject constructor(
    val expenditureRepository: ExpenditureRepository,
    private val spendingRepository: SpendingRepository
) {
    suspend fun calculateExpenditures(budgetId: Int) {
        expenditureRepository.getByBudgetId(budgetId).forEach { expenditure ->
            expenditure.fact = spendingRepository.getByExpenditureId(expenditure.id).sumOf { it.amount }
            expenditureRepository.update(expenditure)
        }
    }

    suspend fun cloneExpenditures(budgetIdFrom: Int, budgetIdTo: Int) {
        with(expenditureRepository) {
            getByBudgetId(budgetIdFrom).forEach { expenditure ->
                val clone = clone(expenditure).also { it.data.budget_id = budgetIdTo }
                insert(clone)
            }
        }
    }

    fun findOrCreate(name: String, budgetId: Int): Expenditure {
        val nameForSearch = name.trim().uppercase()
        val expenditure = expenditureRepository.getByBudgetId(budgetId)
            .firstOrNull { it.name.uppercase() == nameForSearch }
        return expenditure ?: createExpenditure(name, budgetId)
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