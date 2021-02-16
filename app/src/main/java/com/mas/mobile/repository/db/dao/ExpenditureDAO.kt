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
    fun getById(expenditureId: Int): Expenditure

    @Transaction
    @Query("SELECT * FROM expenditures WHERE budget_id = :budgetId")
    fun getByBudgetId(budgetId: Int): List<Expenditure>

    @Ignore
    suspend fun insert(vararg expenditures: Expenditure): List<Long> {
        return insertExpenditureData(*expenditures.map { s -> s.data }.toTypedArray())
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpenditureData(vararg expenditures: ExpenditureData): List<Long>

    @Ignore
    suspend fun update(vararg expenditures: Expenditure) {
        updateExpenditureData(*expenditures.map { s -> s.data }.toTypedArray())
    }

    @Update
    suspend fun updateExpenditureData(vararg expenditures: ExpenditureData)

    @Ignore
    suspend fun delete(expenditure: Expenditure) {
        deleteExpenditureData(expenditure.data)
    }

    @Delete
    suspend fun deleteExpenditureData(expenditure: ExpenditureData)
}