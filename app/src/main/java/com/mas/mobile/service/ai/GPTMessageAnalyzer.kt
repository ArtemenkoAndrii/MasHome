package com.mas.mobile.service.ai

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mas.mobile.domain.budget.ExpenditureName
import com.mas.mobile.domain.message.MessageAnalyzer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GPTMessageAnalyzer @Inject constructor(
    private val gptChatService: GptChatService
): MessageAnalyzer {

    override suspend fun analyze(message: String, availableExpenditures: Set<ExpenditureName>): MessageAnalyzer.Result =
        try {
            doAnalyze(message, availableExpenditures)
        } catch (e: Exception) {
            Log.e(this::class.java.name, "GPT request failed", e)
            MessageAnalyzer.Failed
        }

    private suspend fun doAnalyze(message: String, availableExpenditures: Set<ExpenditureName>) =
        askForMatchers(message)?.let { matcher ->
            val exp = askForExpenditure(message, availableExpenditures)?.get(CATEGORY)?.asString?.lowercase()
            MessageAnalyzer.Rule(
                amountMatcher = matcher.get(PATTERN).asString,
                expenditureMatcher = matcher.get(SELLER).asString,
                expenditureName = availableExpenditures.firstOrNull { it.value.lowercase() == exp }
            )
        } ?: MessageAnalyzer.NoData

    private suspend fun askForMatchers(message: String): JsonObject? =
        ask(MATCHER_CONTEXT, message).extractJSON().firstOrNull { it.has(PATTERN) }

    private suspend fun askForExpenditure(message: String, expenditureName: Set<ExpenditureName>): JsonObject? {
        val expenditures = expenditureName.joinToString(", ") { it.value }
        val request = "Message: $message\n Categories: $expenditures"
        return ask(EXPENDITURE_CONTEXT, request).extractJSON().firstOrNull { it.has(CATEGORY) }
    }

    private suspend fun ask(context: String, question: String): String {
        val request = ChatGPTRequest(listOf(
            ChatGPTRequest.Message("system", context),
            ChatGPTRequest.Message("user", question)
        ))

        Log.d(this::class.simpleName, "GPT request:\n ${request.messages}")
        val response = gptChatService.sendMessage(request)

        return if (response.isSuccessful) {
            val text = response.body()?.let { it.choices[0].message.content }
            Log.d(this::class.simpleName, "GPT response:\n $text")
            text ?: NO_RESPONSE
        } else {
            NO_RESPONSE
        }
    }

    private fun String.extractJSON(): List<JsonObject> {
        val entries = mutableListOf<JsonObject>()

        var startIndex = 0
        var openingBracesCount = 0

        for (i in this.indices) {
            if (this[i] == '{') {
                if (openingBracesCount == 0) {
                    startIndex = i
                }
                openingBracesCount++
            } else if (this[i] == '}') {
                openingBracesCount--
                if (openingBracesCount == 0) {
                    val jsonString = this.substring(startIndex, i + 1)
                    try {
                        val element = JsonParser.parseString(jsonString)
                        if (element.isJsonObject) {
                            entries.add(element.asJsonObject)
                        }
                    } catch (e: Exception) {
                        Log.e(this::class.simpleName, "Invalid JSON: $jsonString", e)
                    }
                }
            }
        }

        return entries
    }

    private companion object {
        const val NO_RESPONSE = ""
        const val PATTERN = "pattern"
        const val SELLER = "seller"
        const val CATEGORY = "category"

        val MATCHER_CONTEXT = """
           For given message provide seller name and generate pattern for capturing spending amount. 
           The pattern should use {amount} as placeholder and must exclude any specific seller and customer data.
           For example for message \"Paid €0,55 at BURGERKING. Your card ending with 1234. Balance 10.00\" 
           expected pattern would be \"Paid €{amount} at\" 
           Reply in JSON format with two field \"$PATTERN\" and \"$SELLER\"(if any)
        """.trimIndent()

        val EXPENDITURE_CONTEXT = """
            Which expense category from given list is best suited for that message?
            Reply in JSON format with one field \"$CATEGORY\" which can be empty if the suggested categories are not suitable
        """.trimIndent()
    }
}