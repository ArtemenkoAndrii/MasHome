package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.Budget

@Dao
interface BudgetDAO {
    @Query("SELECT * FROM budgets ORDER BY startsOn DESC")
    fun getAllLive(): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    fun getByIdLive(budgetId: Int): LiveData<Budget>

    @Query("SELECT * FROM budgets WHERE isActive = 1")
    fun getActive(): Budget

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    fun getById(budgetId: Int): Budget

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg budgets: Budget): List<Long>

    @Update
    suspend fun update(vararg budgets: Budget)

    @Delete
    suspend fun delete(budget: Budget)
}