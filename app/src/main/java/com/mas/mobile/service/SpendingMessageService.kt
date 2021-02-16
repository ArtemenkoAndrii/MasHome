package com.mas.mobile.service

import android.content.Context
import com.mas.mobile.repository.SpendingMessageRepository
import com.mas.mobile.repository.SpendingRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendingMessageService @Inject constructor(
    private val suggestionService: SuggestionService,
    private val spendingMessageRepository: SpendingMessageRepository,
    private val spendingRepository: SpendingRepository,
    context: Context,
) {

    fun processMessage(message: Message) {
        when (val suggestion = suggestionService.makeSuggestions(message)) {
            is AutoSuggestion -> processAutoSuggestion(message, suggestion)
            //is ManualSuggestion -> processManualSuggestion(message, suggestion)
            else -> {}
        }
    }

    private fun processAutoSuggestion(message: Message, suggestion: AutoSuggestion) {
        val spending = spendingRepository.createNew().also {
            it.data.date = message.date
            it.data.amount = suggestion.amount
            it.data.comment = message.text
            it.data.expenditureId = suggestion.expenditureId
        }

        val spendingMessage = spendingMessageRepository.createNew().also {
            it.name = message.sender
            it.message = message.text
            it.receivedAt = message.date
            it.ruleId = suggestion.ruleId
            it.suggestedExpenditureId = suggestion.expenditureId
            it.suggestedAmount = suggestion.amount
        }

        GlobalScope.launch {
            val ids = spendingRepository.insert(spending)
            spendingMessageRepository.insert(spendingMessage.also { it.spendingId = ids.toInt() })
        }
    }

    private fun processManualSuggestion(message: Message, suggestion: ManualSuggestion) {
        val spendingMessage = spendingMessageRepository.createNew().also {
            it.name = message.sender
            it.message = message.text
            it.receivedAt = message.date
            it.ruleId = suggestion.ruleId
            it.suggestedAmount = suggestion.amount
        }

        GlobalScope.launch {
            spendingMessageRepository.insert(spendingMessage)
        }
    }
}
