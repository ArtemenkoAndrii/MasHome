package com.mas.mobile.repository

import android.content.Context
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.Budget

class BudgetRepository {
     companion object {
          lateinit var db: AppDatabase

          val live: Live = Live
          object Live {
               fun getAll() = db.budgetDao().getAllLive()
               fun getById(budgetId: Int) = db.budgetDao().getByIdLive(budgetId)
          }

          fun getActive() = db.budgetDao().getActive()
          fun getById(budgetId: Int) = db.budgetDao().getById(budgetId)

          fun createNew(): Budget {
               return Budget()
          }

          suspend fun insert(vararg budgets: Budget): List<Long> {
               return db.budgetDao().insert(*budgets)
          }

          suspend fun update(vararg budgets: Budget) {
               db.budgetDao().update(*budgets)
          }

          suspend fun delete(budget: Budget) {
               db.budgetDao().delete(budget)
          }

          fun initDb(context: Context) {
               db = AppDatabase.getInstance(context)
          }
     }
}