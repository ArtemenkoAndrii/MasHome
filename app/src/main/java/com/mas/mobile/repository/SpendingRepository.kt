package com.mas.mobile.repository

import android.content.Context
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.repository.db.entity.SpendingData

class SpendingRepository {
     companion object {
          lateinit var db: AppDatabase

          val live: LiveSpendingRepository = LiveSpendingRepository
          object LiveSpendingRepository {
               fun getAll() = db.spendingDao().getAllLive()
               fun getByExpenditureId(expenditure: Int) = db.spendingDao().getByExpenditureIdLive(expenditure)
               fun getByBudgetId(budgetId: Int) = db.spendingDao().getByBudgetIdLive(budgetId)
               fun getById(spendingId: Int) = db.spendingDao().getByIdLive(spendingId)
          }

          fun getById(spendingId: Int) = db.spendingDao().getById(spendingId)

          fun createNew(expenditureId: Int): Spending {
               val expenditure = if (expenditureId > 0) {
                    ExpenditureRepository.getById(expenditureId)
               } else {
                    ExpenditureRepository.createNew(-1)
               }

               return Spending(SpendingData(expenditureId = expenditure.id), expenditure.data)
          }

          suspend fun insert(vararg spendings: Spending) {
               db.spendingDao().insert(*spendings)
          }

          suspend fun update(vararg spendings: Spending) {
               db.spendingDao().update(*spendings)
          }

          suspend fun delete(spending: Spending) {
               db.spendingDao().delete(spending)
          }

          fun initDb(context: Context) {
               db = AppDatabase.getInstance(context)
          }
     }
}