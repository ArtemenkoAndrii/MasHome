package com.mas.mobile.db.dao

import androidx.room.*
import com.mas.mobile.db.entity.CategoryEntity

@Dao
interface CategoryDAO {
    @Query("SELECT * FROM category")
    fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE uid IN (:categoryIds)")
    fun loadAllByIds(categoryIds: IntArray): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): CategoryEntity

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg categories: CategoryEntity)

    @Delete
    fun delete(user: CategoryEntity)
}