package com.mas.mobile.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mas.mobile.domain.message.*
import com.mas.mobile.repository.db.config.AppDatabase
import javax.inject.Singleton

@Singleton
class QualifierRepositoryImpl(val db: AppDatabase) : QualifierRepository {
    private val dao = db.qualifierDAO()

    override val live: QualifierLiveData = object : QualifierLiveData {
        override fun getCatchQualifiers(): LiveData<List<CatchQualifier>> =
            Transformations.map(dao.getCatchQualifiersLive()) { qualifiers ->
                qualifiers.map { CatchQualifier(it.name) }.toList()
            }

        override fun getSkipQualifiers(): LiveData<List<SkipQualifier>> =
            Transformations.map(dao.getSkipQualifiersLive()) { qualifiers ->
                qualifiers.map { SkipQualifier(it.name) }.toList()
            }
    }

    override fun getCatchQualifiers(): List<CatchQualifier> =
        dao.getCatchQualifiers().map { CatchQualifier(it.name) }

    override fun getSkipQualifiers(): List<SkipQualifier> =
        dao.getSkipQualifiers().map { SkipQualifier(it.name) }

    override suspend fun save(item: Qualifier) {
        dao.upsert(item.toDto())
    }

    override suspend fun remove(item: Qualifier) {
        dao.delete(item.toDto())
    }
}