package com.mas.mobile.domain.message

import java.util.Currency

data class MessageTemplate(
    val id: MessageTemplateId,
    var sender: String,
    var pattern: Pattern,
    var example: String,
    var currency: Currency,
    var isEnabled: Boolean
)

@JvmInline
value class MessageTemplateId(val value: Int)