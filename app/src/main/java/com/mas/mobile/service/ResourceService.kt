package com.mas.mobile.service

import android.content.Context
import com.mas.mobile.R
import com.mas.mobile.util.DateTool
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceService @Inject constructor(
    private val context: Context
) {
    fun budgetTruncatedComment(date: LocalDate): String {
        val mask = context.getString(R.string.constant_budget_truncated)
        return String.format(mask, DateTool.dateToString(date))
    }

    fun btnSpendingParseMessage() = context.getString(R.string.btn_spending_parse_message)

    fun budgetChangeDateMessage() = context.getString(R.string.message_budget_date)
    fun budgetRemoveMessage() = context.getString(R.string.message_budget_removing)

    fun messageRuleSave() = context.getString(R.string.message_rule_save)
    fun messageSpendingNotFound() = context.getString(R.string.message_spending_not_found)

    fun messageAreYouSure() = context.getString(R.string.dialog_confirmation_remove)
    fun messageAllowNotifications() = context.getString(R.string.message_allow_notifications)
    fun messageSettingsStartDay() = context.getString(R.string.message_settings_start_day)
    fun messageSettingsPeriod() = context.getString(R.string.message_settings_period)
    fun messageCapturingDisabled() = context.getString(R.string.message_capturing_disabled)

    fun spendingMessageClickToBind() = context.getString(R.string.btn_spending_message_bind)
    fun spendingMessageClickToDiscover() = context.getString(R.string.btn_spending_message_discover)

    fun constantToday() = context.getString(R.string.constant_today)
    fun constantYesterday() = context.getString(R.string.constant_yesterday)

    fun constantPeriodWeek() = context.getString(R.string.constant_period_week)
    fun constantPeriodTwoWeeks() = context.getString(R.string.constant_period_two_weeks)
    fun constantPeriodMonths() = context.getString(R.string.constant_period_month)
    fun constantPeriodQuarter() = context.getString(R.string.constant_period_quarter)
    fun constantPeriodYear() = context.getString(R.string.constant_period_year)

    fun dialogConfirmationOk() = context.getString(R.string.dialog_confirmation_ok)
    fun dialogConfirmationCancel() = context.getString(R.string.dialog_confirmation_cancel)
    fun dialogCurrencyTitle() = context.getString(R.string.dialog_currency_title)

    fun tabQualifierCatch() = context.getString(R.string.tab_qualifier_catch)
    fun tabQualifierSkip() = context.getString(R.string.tab_qualifier_skip)

    fun notificationNewVersionTitle() = context.getString(R.string.notification_new_version_title)
    fun notificationNewVersionText() = context.getString(R.string.notification_new_version_text)
    fun notificationUpdate() = context.getString(R.string.notification_update)
    fun notificationIgnore() = context.getString(R.string.notification_ignore)
}
