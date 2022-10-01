package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mas.mobile.BuildConfig
import com.mas.mobile.model.DayOfMonth
import com.mas.mobile.model.Period
import com.mas.mobile.model.Period.*
import com.mas.mobile.repository.SettingsRepository
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.SettingsService
import com.mas.mobile.util.DateTool
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.DayOfWeek

class SettingsViewModel @AssistedInject constructor(
    private val settingsRepository: SettingsRepository,
    private val coroutineService: CoroutineService,
    private val settingsService: SettingsService
) : ViewModel() {
    private var item = settingsRepository.get()

    var period = MutableLiveData<Period>()
    var startDayOfMonth = MutableLiveData<Int>()
    var startDayOfMonthVisible = MutableLiveData<Boolean>()
    var startDayOfWeek = MutableLiveData<Int>()
    var startDayOfWeekVisible = MutableLiveData<Boolean>()
    var captureSms = MutableLiveData<Boolean>()
    var captureNotifications = MutableLiveData<Boolean>()
    val appVersion = BuildConfig.VERSION_NAME

    private var isNotificationsAllowed: () -> Boolean = { true }
    private var isSMSAllowed: () -> Boolean = { true }
    private val dayOfWeekMap = listOf(
        DayOfWeek.MONDAY to DateTool.formatToDayOfWeek(DayOfWeek.MONDAY),
        DayOfWeek.TUESDAY to DateTool.formatToDayOfWeek(DayOfWeek.TUESDAY),
        DayOfWeek.WEDNESDAY to DateTool.formatToDayOfWeek(DayOfWeek.WEDNESDAY),
        DayOfWeek.THURSDAY to DateTool.formatToDayOfWeek(DayOfWeek.THURSDAY),
        DayOfWeek.FRIDAY to DateTool.formatToDayOfWeek(DayOfWeek.FRIDAY),
        DayOfWeek.SATURDAY to DateTool.formatToDayOfWeek(DayOfWeek.SATURDAY),
        DayOfWeek.SUNDAY to DateTool.formatToDayOfWeek(DayOfWeek.SUNDAY)
    )

    val availableDaysOfMonth = (1..28).toList()
    val availableDaysOfWeek = dayOfWeekMap.map { it.second }.toList()

    init {
        afterLoad()
    }

    fun isThisFirstLaunch() = settingsService.isThisFirstLaunch()

    fun save() {
        coroutineService.backgroundTask {
            settingsRepository.update(item)
        }
    }

    private fun afterLoad() {
        period.value = item.period
        period.observeForever {
            item.period = it
            switchDateFields(it)
        }

        startDayOfMonth.value = availableDaysOfMonth.indexOf(item.startDayOfMonth.value)
        startDayOfMonth.observeForever {
            item.startDayOfMonth = DayOfMonth(availableDaysOfMonth[it])
        }

        startDayOfWeek.value = dayOfWeekMap.withIndex().filter { it.value.first == item.startDayOfWeek }.map { it.index }.first()
        startDayOfWeek.observeForever {
            item.startDayOfWeek = dayOfWeekMap[it].first
        }

        captureSms.value = item.captureSms
        captureSms.observeForever {
            if (!isSMSAllowed() && it == true) {
                captureSms.value = false
            } else {
                item.captureSms = it
            }
        }

        captureNotifications.value = item.captureNotifications
        captureNotifications.observeForever {
            if (!isNotificationsAllowed() && it == true) {
                captureNotifications.value = false
            } else {
                item.captureNotifications = it
            }
        }
    }

    fun setMonthPeriod() {
        period.value = MONTH
    }

    fun setWeekPeriod() {
        period.value = WEEK
    }

    fun set2Week2Period() {
        period.value = TWO_WEEKS
    }

    fun setQuarterPeriod() {
        period.value = QUARTER
    }

    fun setYearPeriod() {
        period.value = YEAR
    }

    fun registerPermissionValidators(
        smsPermissionsValidator: () -> Boolean = { true },
        notificationsPermissionsValidator: () -> Boolean = {true}) {
        isSMSAllowed = smsPermissionsValidator
        isNotificationsAllowed = notificationsPermissionsValidator
    }

    private fun switchDateFields(period: Period) {
        startDayOfMonthVisible.value = false
        startDayOfWeekVisible.value = false

        when (period) {
            MONTH -> startDayOfMonthVisible.value = true
            WEEK, TWO_WEEKS -> startDayOfWeekVisible.value = true
            else -> {}
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): SettingsViewModel
    }
}
