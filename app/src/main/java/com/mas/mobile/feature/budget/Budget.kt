package com.mas.mobile.feature.budget

import com.mas.mobile.repository.db.entity.Expenditure
import java.time.LocalDate

data class Budget(
    var id: Int,
    var name: String,
    var plan: Double,
    var fact: Double,
    var startsOn: LocalDate,
    var lastDayAt: LocalDate,
    var isActive: Boolean,
    var comment: String?,
    var expenditures: List<Expenditure>
)