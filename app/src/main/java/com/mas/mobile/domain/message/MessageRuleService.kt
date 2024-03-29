package com.mas.mobile.domain.message

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRuleService @Inject constructor(
    private val messageAnalyzer: MessageAnalyzer,
    val ruleRepository: MessageRuleRepository
) {
    suspend fun generateRuleFromMessage(message: Message): MessageRule? {
        val pattern = messageAnalyzer.buildPattern(message.text)

        return when (val result = pattern?.parse(message.text)) {
            is Pattern.Data -> ruleRepository.create().also {
                it.name = message.sender
                it.pattern = pattern
                it.expenditureMatcher = result.merchant ?: ""
                it.expenditureName = ""
            }
            else -> {
                Log.d(this::class.simpleName, "Can't generate rule for message $message")
                null
            }
        }
    }

    fun evaluateRuleChanges(message: Message?, rule: MessageRule?, expName: String): MessageRule? {
        if (message == null || rule == null) {
            return null
        }

        val merchant = (rule.evaluate(message.sender, message.text) as? MessageRule.Match)
            ?.merchant ?: return null

        return when {
            merchant == rule.expenditureMatcher && expName != rule.expenditureName -> {
                // Update rule with new expenditure name
                rule.also {
                    it.expenditureName = expName
                }
            }
            merchant.isNotBlank() && expName != rule.expenditureName -> {
                // Create new rule based on a new merchant
                ruleRepository.create().also {
                    it.name = rule.name
                    it.pattern = rule.pattern
                    it.expenditureMatcher = merchant
                    it.expenditureName = expName
                }
            }
            else -> null
        }
    }
}