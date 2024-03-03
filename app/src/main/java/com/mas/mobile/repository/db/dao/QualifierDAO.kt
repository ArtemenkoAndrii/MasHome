package com.mas.mobile.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mas.mobile.repository.db.entity.Qualifier

@Dao
interface QualifierDAO {
    @Query("SELECT * FROM qualifiers WHERE type=${Qualifier.CATCH} ORDER BY name")
    fun getCatchQualifiersLive(): LiveData<List<Qualifier>>

    @Query("SELECT * FROM qualifiers WHERE type=${Qualifier.SKIP} ORDER BY name")
    fun getSkipQualifiersLive(): LiveData<List<Qualifier>>

    @Query("SELECT * FROM qualifiers WHERE type=${Qualifier.CATCH}")
    fun getCatchQualifiers(): List<Qualifier>

    @Query("SELECT * FROM qualifiers WHERE type=${Qualifier.SKIP}")
    fun getSkipQualifiers(): List<Qualifier>

    @Insert
    suspend fun insert(qualifier: Qualifier): Long

    @Update
    suspend fun update(qualifier: Qualifier)

    @Delete
    suspend fun delete(qualifier: Qualifier)

    @Upsert
    suspend fun upsert(qualifier: Qualifier): Long
}