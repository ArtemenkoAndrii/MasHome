package com.mas.mobile.feature.expenditure

import com.mas.mobile.feature.budget.Budget

data class Expenditure (
    var id: Int,
    var name: String,
    var plan: Double,
    var fact: Double,
    var comment: String?,
    var budget: Budget
)