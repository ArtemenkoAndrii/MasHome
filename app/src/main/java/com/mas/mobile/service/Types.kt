package com.mas.mobile.service

import java.time.LocalDateTime

data class Message(
    val sender: String,
    val date: LocalDateTime,
    val text: String,
)

sealed interface Suggestion
data class AutoSuggestion(val ruleId: Int, val expenditureId: Int, val amount: Double): Suggestion
data class ManualSuggestion(val ruleId: Int, val amount: Double): Suggestion
object NoSuggestion : Suggestion
