package com.mas.mobile.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mas.mobile.db.entity.CategoryEntity

@Dao
interface CategoryDAO {
    @Query("SELECT * FROM category")
    fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE uid IN (:categoryIds)")
    fun loadAllByIds(categoryIds: IntArray): List<CategoryEntity>

    @Query("SELECT * FROM category WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): CategoryEntity

    @Insert
    fun insertAll(vararg categories: CategoryEntity)

    @Delete
    fun delete(user: CategoryEntity)
}