package com.mas.mobile.service

import android.content.Context
import com.mas.mobile.R
import com.mas.mobile.util.DateTool
import java.time.LocalDate
import javax.inject.Inject

class ResourceService @Inject constructor(
    private val context: Context
) {
    fun budgetTruncatedComment(date: LocalDate): String {
        val mask = context.getString(R.string.constant_budget_truncated)
        return String.format(mask, DateTool.dateToString(date))
    }

    fun budgetChangeDateMessage() = context.getString(R.string.message_budget_date)
    fun budgetRemoveMessage() = context.getString(R.string.message_budget_removing)

    fun messageSettingsFirstLaunch() = context.getString(R.string.message_settings_first_launch)
    fun messageExpenditureFirstLaunch() = context.getString(R.string.message_expenditures_first_launch)
    fun messageAreYouSure() = context.getString(R.string.dialog_confirmation_remove)
    fun messageAllowSms() = context.getString(R.string.message_allow_sms)
    fun messageAllowNotifications() = context.getString(R.string.message_allow_notifications)

    fun spendingMessageClickToAdd() = context.getString(R.string.label_spending_message_click)

    fun constantToday() = context.getString(R.string.constant_today)
    fun constantYesterday() = context.getString(R.string.constant_yesterday)
}
