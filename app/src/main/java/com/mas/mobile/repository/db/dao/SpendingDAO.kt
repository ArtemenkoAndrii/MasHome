package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.Spending
import com.mas.mobile.repository.db.entity.SpendingData

@Dao
interface SpendingDAO {
    @Transaction
    @Query("SELECT * FROM spendings")
    fun getAllLive(): LiveData<List<Spending>>

    @Transaction
    @Query("SELECT * FROM spendings WHERE expenditure_id = :expenditureId ORDER BY date DESC")
    fun getByExpenditureIdLive(expenditureId: Int): LiveData<List<Spending>>

    @Transaction
    @Query("SELECT s.* FROM spendings s INNER JOIN expenditures e ON s.expenditure_id = e.id WHERE e.budget_id = :budgetId ORDER BY s.date DESC")
    fun getByBudgetIdLive(budgetId: Int): LiveData<List<Spending>>

    @Transaction
    @Query("SELECT * FROM spendings WHERE id = :spendingId")
    fun getByIdLive(spendingId: Int): LiveData<Spending>

    @Transaction
    @Query("SELECT * FROM spendings WHERE id = :spendingId")
    fun getById(spendingId: Int): Spending

    @Ignore
    suspend fun insert(spending: Spending) =
        insertSpendingData(spending.data)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpendingData(spendings: SpendingData): Long

    @Ignore
    suspend fun update(spending: Spending) {
        updateSpendingData(spending.data)
    }

    @Update
    suspend fun updateSpendingData(spendings: SpendingData)

    @Ignore
    suspend fun delete(spending: Spending) {
        deleteSpendingData(spending.data)
    }

    @Delete
    suspend fun deleteSpendingData(spending: SpendingData)
}