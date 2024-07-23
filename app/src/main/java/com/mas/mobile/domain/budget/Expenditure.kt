package com.mas.mobile.domain.budget

data class Expenditure (
    val id: ExpenditureId,
    var name: String,
    var categoryId: CategoryId,
    var plan: Double,
    var fact: Double,
    var comment: String,
    var budgetId: BudgetId
)

@JvmInline
value class ExpenditureId(val value: Int)

@JvmInline
value class ExpenditureName(val value: String)