package com.mas.mobile.repository

import com.mas.mobile.repository.db.BaseRepository
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.db.config.converter.SQLiteTypeConverter
import com.mas.mobile.repository.db.entity.Budget
import com.mas.mobile.service.BudgetService
import com.mas.mobile.util.DateTool
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
     db: AppDatabase
) : BaseRepository<Budget> {
     private val converter = SQLiteTypeConverter()
     val live = Live(db)
     class Live(val db: AppDatabase) {
          fun getAll() = db.budgetDao().getAllLive()
          fun getById(budgetId: Int) = db.budgetDao().getByIdLive(budgetId)
     }

     private val dao = db.budgetDao()

     override fun getById(id: Int): Budget? = dao.getById(id)

     override fun clone(item: Budget) = item.copy(id = 0)

     override fun createNew(): Budget = Budget()

     fun getActive() = dao.getActiveOn(LocalDate.now())

     fun getLastCompletedOn(date: LocalDate) = dao.getLatestEndedOn(date)

     override suspend fun insert(item: Budget) = dao.insert(item)

     override suspend fun update(item: Budget) {
          dao.update(item)
     }

     override suspend fun delete(item: Budget) {
          dao.delete(item)
     }

     private fun LocalDate.toLong() = DateTool.localDateToLong(this)
}
