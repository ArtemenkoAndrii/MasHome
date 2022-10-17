package com.mas.mobile.repository

import androidx.room.withTransaction
import com.mas.mobile.model.DayOfMonth
import com.mas.mobile.model.Period
import com.mas.mobile.model.Settings
import com.mas.mobile.repository.db.config.AppDatabase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import com.mas.mobile.repository.db.entity.Settings as SettingsEntity

@Singleton
class SettingsRepository  @Inject constructor(
    private val db: AppDatabase
) {
    fun get(): Settings {
        val settings = Settings()
        getValue(PERIOD)?.let { settings.period = Period.valueOf(it) }
        getValue(START_DAY_OF_MONTH)?.let { settings.startDayOfMonth = DayOfMonth(it.toInt()) }
        getValue(START_DAY_OF_WEEK)?.let { settings.startDayOfWeek = DayOfWeek.of(it.toInt()) }
        getValue(CAPTURE_SMS)?.let { settings.captureSms = it.toBoolean() }
        getValue(CAPTURE_NOTIFICATIONS)?.let { settings.captureNotifications = it.toBoolean() }
        return  settings
    }

    suspend fun update(settings: Settings) {
        db.withTransaction {
            merge(PERIOD, settings.period.toString())
            merge(START_DAY_OF_MONTH, settings.startDayOfMonth.value.toString())
            merge(START_DAY_OF_WEEK, settings.startDayOfWeek.value.toString())
            merge(CAPTURE_SMS, settings.captureSms.toString())
            merge(CAPTURE_NOTIFICATIONS, settings.captureNotifications.toString())
        }
    }

    private fun getValue(key: String) =
        db.settingsDao().getByKey(key)?.value

    private suspend fun merge(key: String, value: String?) {
        val entity = SettingsEntity(key, value)
        if (getValue(key) != null) {
            db.settingsDao().update(entity)
        } else {
            db.settingsDao().insert(entity)
        }
    }

    companion object {
        const val PERIOD = "budget.period"
        const val START_DAY_OF_MONTH = "budget.startDayOfMonth"
        const val START_DAY_OF_WEEK = "budget.startDayOfWeek"
        const val CAPTURE_SMS = "budget.captureSMS"
        const val CAPTURE_NOTIFICATIONS = "budget.captureNotifications"
    }
}
