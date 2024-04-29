package com.mas.mobile.repository.budget

import com.mas.mobile.domain.budget.ExchangeInfo
import com.mas.mobile.domain.budget.Expenditure
import com.mas.mobile.domain.budget.Spending
import com.mas.mobile.domain.budget.SpendingId
import com.mas.mobile.repository.db.entity.SpendingData
import java.util.Currency
import java.util.Locale

object SpendingMapper {
    fun toModel(dto: SpendingData, expenditure: Expenditure) =
        Spending(
            id = SpendingId(dto.id),
            comment = dto.comment ?: "",
            date = dto.date,
            amount = dto.amount,
            expenditure = expenditure,
            exchangeInfo = dto.foreignAmount?.let {
                ExchangeInfo(
                    rawAmount = it ,
                    rate = dto.rate ?: 0.00,
                    currency = Currency.getInstance(dto.currency) ?: Currency.getInstance(Locale.getDefault())
                )
            }
        )

    fun toDto(model: Spending) =
        SpendingData(
            id = model.id.value,
            comment = model.comment,
            date = model.date,
            amount = model.amount,
            expenditureId = model.expenditure.id.value,
            currency = model.exchangeInfo?.currency?.currencyCode,
            rate = model.exchangeInfo?.rate,
            foreignAmount = model.exchangeInfo?.rawAmount
        )
}

fun SpendingData.toModel(expenditure: Expenditure) =
    SpendingMapper.toModel(this, expenditure)

fun Spending.toDto() = SpendingMapper.toDto(this)