package com.mas.mobile.domain.message

import com.mas.mobile.domain.budget.ExpenditureName

interface MessageAnalyzer {
    suspend fun analyze(message: String, availableExpenditures: Set<ExpenditureName>): Result

    sealed class Result

    class Rule(
        val amountMatcher: String,
        val expenditureMatcher: String,
        val expenditureName: ExpenditureName?
    ) : Result()

    object NoData : Result()

    object Failed : Result()
}

