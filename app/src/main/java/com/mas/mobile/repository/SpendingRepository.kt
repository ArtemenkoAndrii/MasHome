package com.mas.mobile.repository

import com.mas.mobile.repository.db.BaseRepository
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.ExpenditureData
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.repository.db.entity.SpendingData
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import javax.inject.Inject

class SpendingRepository @Inject constructor(
     private val db: AppDatabase
) : BaseRepository<Spending> {
     val live = Live(db)
     class Live(val db: AppDatabase) {
          fun getById(id: Int) = db.spendingDao().getByIdLive(id)
          fun getByBudgetId(budgetId: Int) = db.spendingDao().getByBudgetIdLive(budgetId)
          fun getByExpenditureId(expenditure: Int) = db.spendingDao().getByExpenditureIdLive(expenditure)
     }

     override fun getById(id: Int) = db.spendingDao().getById(id)

     override fun clone(item: Spending) = item.copy(data = item.data.copy(id = 0))

     override fun createNew() = Spending(data = SpendingData(), expenditure = ExpenditureData())

     override suspend fun insert(item: Spending): Long =
          updateDependencies(item) {
               db.spendingDao().insert(it)
          }

     override suspend fun update(item: Spending) {
          updateDependencies(item) {
               db.spendingDao().update(it).run { 0 }
          }
     }

     override suspend fun delete(item: Spending) {
          db.spendingDao().delete(item)
     }

     private suspend fun updateDependencies(item: Spending, save: suspend(param: Spending) -> Long) =
          if (item.expenditureId == AppDatabase.AUTOGENERATED) {
               db.runInTransaction(Callable {
                    runBlocking {
                         val generatedId = db.expenditureDao().insertExpenditureData(item.expenditure).toInt()
                         save(item.also { it.expenditureId = generatedId })
                    }
               })
          } else {
               save(item)
          }
}