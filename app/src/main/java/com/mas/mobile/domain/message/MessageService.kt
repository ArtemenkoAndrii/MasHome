package com.mas.mobile.domain.message

import android.util.Log
import com.mas.mobile.domain.budget.BudgetService
import com.mas.mobile.service.CoroutineService
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageService @Inject constructor(
    private val suggestionService: SuggestionService,
    private val coroutineService: CoroutineService,
    private val budgetService: BudgetService,
    private val messageRepository: MessageRepository
) {

    fun handleMessage(sender: String, text: String, date: LocalDateTime) {
        val validSender = sender.trim()
        val validText = text.trim()
        when (val suggestion = suggestionService.makeSuggestions(validSender, validText)) {
            is ManualSuggestion -> createMessage(validSender, validText, date, suggestion) {}
            is AutoSuggestion -> {
                createMessage(validSender, validText, date, suggestion) { message ->
                    val spendingId = budgetService.spend(
                        date = message.receivedAt,
                        amount = message.suggestedAmount,
                        comment = message.text,
                        expenditureName = message.suggestedExpenditureName ?: ""
                    )
                    message.spendingId = spendingId
                }
            }
            else -> Log.i(this::class.simpleName, "A message from $validSender doesn't match any rule.")
        }
    }

    private fun createMessage(sender: String, text: String, date: LocalDateTime, suggestion: ManualSuggestion, hook: suspend (Message) -> Unit) {
        val message = messageRepository.create().also {
            it.sender = sender
            it.text = text
            it.receivedAt = date
            it.ruleId = suggestion.ruleId
            it.suggestedAmount = suggestion.amount
        }

        if (suggestion is AutoSuggestion) {
            message.suggestedExpenditureName = suggestion.expenditureName
        }

        coroutineService.backgroundTask {
            hook(message)
            messageRepository.save(message)
        }
    }
}
