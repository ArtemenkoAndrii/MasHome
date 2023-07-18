package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.ExpenditureRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRuleService @Inject constructor(
    private val messageAnalyzer: MessageAnalyzer,
    private val expenditureRepository: ExpenditureRepository,
    val ruleRepository: MessageRuleRepository
) {
    suspend fun buildRule(sender: String, text: String): MessageRule? {
        val availableExpenditures = expenditureRepository.getExpenditureNames(true, LIMIT)
        return when (val result = messageAnalyzer.analyze(text, availableExpenditures)) {
            is MessageAnalyzer.Rule -> {
                ruleRepository.create().also {
                    it.name = sender
                    it.amountMatcher = result.amountMatcher
                    it.expenditureMatcher = result.expenditureMatcher
                    it.expenditureName = result.expenditureName?.value ?: ""
                }
            }
            else -> {
                Log.i(this::class.simpleName, "Can't generate rule for message $text")
                null
            }
        }
    }

    companion object {
        const val LIMIT: Short = 20
    }
}