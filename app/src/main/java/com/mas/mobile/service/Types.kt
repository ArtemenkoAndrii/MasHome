package com.mas.mobile.service

import java.time.LocalDateTime

data class Message(
    val sender: String,
    val date: LocalDateTime,
    val text: String,
)

sealed interface Suggestion

data class AutoSuggestion(
    val ruleId: Int,
    val expenditureName: String,
    val amount: Double,
    val time: LocalDateTime): Suggestion

data class ManualSuggestion(
    val ruleId: Int,
    val amount: Double,
    val time: LocalDateTime): Suggestion

object NoSuggestion : Suggestion
