package com.mas.mobile.domain.message

sealed interface Suggestion

open class ManualSuggestion(
    val ruleId: MessageRuleId,
    val amount: Double
): Suggestion

class AutoSuggestion(
    ruleId: MessageRuleId,
    amount: Double,
    val expenditureName: String,
): ManualSuggestion(ruleId, amount)

object NoSuggestion : Suggestion
