package com.mas.mobile.repository.message

import com.mas.mobile.domain.message.CatchQualifier
import com.mas.mobile.domain.message.Qualifier
import com.mas.mobile.domain.message.SkipQualifier
import com.mas.mobile.repository.db.entity.Qualifier as QualifierData

fun QualifierData.toModel() =
    if (this.type == QualifierData.CATCH) {
        CatchQualifier(this.name)
    } else {
        SkipQualifier(this.name)
    }

fun Qualifier.toDto() =
    if (this is CatchQualifier) {
        QualifierData(this.value, QualifierData.CATCH)
    } else {
        QualifierData(this.value, QualifierData.SKIP)
    }