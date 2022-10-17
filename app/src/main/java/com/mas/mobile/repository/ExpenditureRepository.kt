package com.mas.mobile.repository

import com.mas.mobile.repository.db.BaseRepository
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.ExpenditureData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenditureRepository @Inject constructor(
     private val db: AppDatabase
) : BaseRepository<Expenditure> {
     val live = Live(db)
     class Live(val db: AppDatabase) {
          fun getByBudgetId(budgetId: Int) = db.expenditureDao().getByBudgetIdLive(budgetId)
          fun getUniqueNames() = db.expenditureDao().getUniqueNamesLive()
          fun getById(expenditureId: Int) = db.expenditureDao().getByIdLive(expenditureId)
     }

     override fun getById(id: Int) = db.expenditureDao().getById(id)

     override fun clone(item: Expenditure) = item.copy(data = item.data.copy(id = 0))

     override fun createNew() = Expenditure(data = ExpenditureData(), budget = Budget())

     fun getByBudgetId(budgetId: Int) = db.expenditureDao().getByBudgetId(budgetId)

     fun getByName(name: String, budgetId: Int) = db.expenditureDao().getByName(name, budgetId)

     override suspend fun insert(item: Expenditure): Long =
          db.expenditureDao().insert(item)

     override suspend fun update(item: Expenditure) =
          db.expenditureDao().update(item)

     override suspend fun delete(item: Expenditure) {
          db.expenditureDao().delete(item)
     }
}