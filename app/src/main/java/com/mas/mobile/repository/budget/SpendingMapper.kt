package com.mas.mobile.repository.budget

import com.mas.mobile.domain.budget.*
import com.mas.mobile.repository.db.entity.SpendingData

object SpendingMapper {
    fun toModel(dto: SpendingData, expenditure: Expenditure) =
        Spending(
            id = SpendingId(dto.id),
            comment = dto.comment ?: "",
            date = dto.date,
            amount = dto.amount,
            expenditure = expenditure
        )

    fun toDto(model: Spending) =
        SpendingData(
            id = model.id.value,
            comment = model.comment,
            date = model.date,
            amount = model.amount,
            expenditureId = model.expenditure.id.value
        )
}

fun SpendingData.toModel(expenditure: Expenditure) =
    SpendingMapper.toModel(this, expenditure)

fun Spending.toDto() = SpendingMapper.toDto(this)