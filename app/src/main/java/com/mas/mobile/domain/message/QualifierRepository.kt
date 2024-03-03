package com.mas.mobile.domain.message

import androidx.lifecycle.LiveData
import com.mas.mobile.domain.Repository

interface QualifierRepository : Repository<Qualifier> {
    val live: QualifierLiveData

    fun getCatchQualifiers(): List<CatchQualifier>
    fun getSkipQualifiers(): List<SkipQualifier>
}

interface QualifierLiveData {
    fun getCatchQualifiers(): LiveData<List<CatchQualifier>>
    fun getSkipQualifiers(): LiveData<List<SkipQualifier>>
}
