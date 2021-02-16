package com.mas.mobile.repository

import android.content.Context
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.ExpenditureData

class ExpenditureRepository {
     companion object {
          lateinit var db: AppDatabase

          val live: LiveExpenditureRepository = LiveExpenditureRepository
          object LiveExpenditureRepository {
               fun getAll() = db.expenditureDao().getAllLive()
               fun getByBudgetId(budgetId: Int) = db.expenditureDao().getByBudgetIdLive(budgetId)
               fun getById(expenditureId: Int) = db.expenditureDao().getByIdLive(expenditureId)
               fun getUniqueNames() = db.expenditureDao().getUniqueNamesLive()
          }

          fun createNew(budgetId: Int = -1): Expenditure {
               val budget = if (budgetId > 0) {
                    BudgetRepository.getById(budgetId)
               } else {
                    BudgetRepository.getActive()
               }
               return Expenditure(ExpenditureData(budget_id = budget.id), budget)
          }

          fun getById(expenditureId: Int) = db.expenditureDao().getById(expenditureId)
          fun getByBudgetId(budgetId: Int) = db.expenditureDao().getByBudgetId(budgetId)

          suspend fun insert(vararg expenditures: Expenditure): List<Long> {
               return db.expenditureDao().insert(*expenditures)
          }

          suspend fun update(vararg expenditures: Expenditure) {
               db.expenditureDao().update(*expenditures)
          }

          suspend fun delete(budget: Expenditure) {
               db.expenditureDao().delete(budget)
          }

          fun initDb(context: Context) {
               db = AppDatabase.getInstance(context)
               BudgetRepository.initDb(context)
          }
     }
}