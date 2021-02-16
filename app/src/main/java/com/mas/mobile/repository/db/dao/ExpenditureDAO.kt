package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.Expenditure
import com.mas.mobile.repository.db.entity.ExpenditureData

@Dao
interface ExpenditureDAO {
    @Transaction
    @Query("SELECT * FROM expenditures")
    fun getAllLive(): LiveData<List<Expenditure>>

    @Transaction
    @Query("SELECT DISTINCT 1 `id`, `name`, 0 `plan`, 0 `fact`, '' `comment`, 1 `budget_id` FROM expenditures e")
    fun getUniqueNamesLive(): LiveData<List<Expenditure>>

    @Transaction
    @Query("SELECT * FROM expenditures WHERE budget_id = :budgetId")
    fun getByBudgetIdLive(budgetId: Int): LiveData<List<Expenditure>>

    @Transaction
    @Query("SELECT * FROM expenditures WHERE id = :expenditureId")
    fun getByIdLive(expenditureId: Int): LiveData<Expenditure>

    @Transaction
    @Query("SELECT * FROM expenditures WHERE id = :expenditureId")
    fun getById(expenditureId: Int): Expenditure?

    @Query("SELECT * FROM expenditures WHERE name = :name AND budget_id = :budgetId")
    fun getByName(name: String, budgetId: Int): Expenditure?

    @Transaction
    @Query("SELECT * FROM expenditures WHERE budget_id = :budgetId")
    fun getByBudgetId(budgetId: Int): List<Expenditure>

    @Ignore
    suspend fun insert(expenditures: Expenditure): Long {
        return insertExpenditureData(expenditures.data)
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpenditureData(expenditures: ExpenditureData): Long

    @Ignore
    suspend fun update(expenditures: Expenditure) {
        updateExpenditureData(expenditures.data)
    }

    @Update
    suspend fun updateExpenditureData(expenditures: ExpenditureData)

    @Ignore
    suspend fun delete(expenditure: Expenditure) {
        deleteExpenditureData(expenditure.data)
    }

    @Delete
    suspend fun deleteExpenditureData(expenditure: ExpenditureData)
}