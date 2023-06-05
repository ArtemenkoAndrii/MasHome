package com.mas.mobile.repository.settings

import com.mas.mobile.domain.settings.DayOfMonth
import com.mas.mobile.domain.settings.Period
import com.mas.mobile.domain.settings.Settings
import java.time.DayOfWeek
import com.mas.mobile.repository.db.entity.Settings as SettingsData

object SettingsMapper {
    fun toModel(dto: List<SettingsData>): Settings {
        val settings = Settings()
        getValue(PERIOD, dto)?.let { settings.period = Period.valueOf(it) }
        getValue(START_DAY_OF_MONTH, dto)?.let { settings.startDayOfMonth = DayOfMonth(it.toInt()) }
        getValue(START_DAY_OF_WEEK, dto)?.let { settings.startDayOfWeek = DayOfWeek.of(it.toInt()) }
        getValue(CAPTURE_SMS, dto)?.let { settings.captureSms = it.toBoolean() }
        getValue(CAPTURE_NOTIFICATIONS, dto)?.let { settings.captureNotifications = it.toBoolean() }
        getValue(POLICY_VERSION, dto)?.let { settings.policyVersion = it }
        return settings
    }

    fun toDto(model: Settings): List<SettingsData> =
        mutableListOf<SettingsData>().also {
            it.setValue(PERIOD, model.period.toString())
            it.setValue(START_DAY_OF_MONTH, model.startDayOfMonth.value.toString())
            it.setValue(START_DAY_OF_WEEK, model.startDayOfWeek.value.toString())
            it.setValue(CAPTURE_SMS, model.captureSms.toString())
            it.setValue(CAPTURE_NOTIFICATIONS, model.captureNotifications.toString())
            it.setValue(POLICY_VERSION, model.policyVersion)
        }

    private fun MutableList<SettingsData>.setValue(key: String, value: String?) {
        this.add(SettingsData(key, value))
    }

    private fun getValue(key: String, dto: List<SettingsData>) =
        dto.firstOrNull { it.key == key }?.value

    private const val PERIOD = "budget.period"
    private const val START_DAY_OF_MONTH = "budget.startDayOfMonth"
    private const val START_DAY_OF_WEEK = "budget.startDayOfWeek"
    private const val CAPTURE_SMS = "budget.captureSMS"
    private const val CAPTURE_NOTIFICATIONS = "budget.captureNotifications"
    private const val POLICY_VERSION = "policy.version"
}