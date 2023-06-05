package com.mas.mobile.repository.budget

import com.mas.mobile.domain.budget.BudgetId
import com.mas.mobile.domain.budget.Expenditure
import com.mas.mobile.domain.budget.ExpenditureId
import com.mas.mobile.repository.db.entity.ExpenditureData

object ExpenditureMapper {
    fun toModel(dto: ExpenditureData) =
        Expenditure(
            id = ExpenditureId(dto.id),
            name = dto.name,
            plan = dto.plan,
            fact = dto.fact,
            comment = dto.comment ?: "",
            budgetId = BudgetId(dto.budget_id)
        )

    fun toDto(model: Expenditure) =
        ExpenditureData(
            id = model.id.value,
            name = model.name,
            plan = model.plan,
            fact = model.fact,
            comment = model.comment,
            budget_id = model.budgetId.value
        )
}

fun ExpenditureData.toModel() = ExpenditureMapper.toModel(this)
fun Expenditure.toDto() = ExpenditureMapper.toDto(this)