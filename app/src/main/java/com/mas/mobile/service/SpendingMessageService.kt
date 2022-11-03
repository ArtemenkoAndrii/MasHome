package com.mas.mobile.service

import com.mas.mobile.repository.SpendingMessageRepository
import com.mas.mobile.repository.SpendingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpendingMessageService @Inject constructor(
    private val suggestionService: SuggestionService,
    private val budgetService: BudgetService,
    private val spendingMessageRepository: SpendingMessageRepository,
    private val spendingRepository: SpendingRepository,
    private val expenditureService: ExpenditureService,
    private val coroutineService: CoroutineService
) {

    fun processMessage(message: Message) {
        when (val suggestion = suggestionService.makeSuggestions(message)) {
            is AutoSuggestion -> processAutoSuggestion(message, suggestion)
            is ManualSuggestion -> processManualSuggestion(message, suggestion)
            else -> {}
        }
    }

    private fun processAutoSuggestion(message: Message, suggestion: AutoSuggestion) {
        val expenditure = expenditureService.findOrCreate(suggestion.expenditureName, budgetService.getActiveOrCreate().id)

        val spending = spendingRepository.createNew().also {
            it.data.date = message.date
            it.data.amount = suggestion.amount
            it.data.comment = message.text
            it.data.expenditureId = expenditure.id
        }

        val spendingMessage = spendingMessageRepository.createNew().also {
            it.name = message.sender
            it.message = message.text
            it.receivedAt = message.date
            it.ruleId = suggestion.ruleId
            it.suggestedExpenditureName = expenditure.name
            it.suggestedAmount = suggestion.amount
        }

        coroutineService.backgroundTask {
            val id = spendingRepository.insert(spending)
            spendingMessageRepository.insert(spendingMessage.also { it.spendingId = id.toInt() })
            budgetService.calculateBudget()
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

        coroutineService.backgroundTask {
            spendingMessageRepository.insert(spendingMessage)
        }
    }
}
