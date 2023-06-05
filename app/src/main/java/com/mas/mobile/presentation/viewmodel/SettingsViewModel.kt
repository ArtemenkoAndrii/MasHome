package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.BuildConfig
import com.mas.mobile.domain.settings.DayOfMonth
import com.mas.mobile.domain.settings.Period
import com.mas.mobile.domain.settings.Settings
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.PermissionService
import com.mas.mobile.domain.settings.SettingsService
import com.mas.mobile.util.DateTool
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.DayOfWeek

class SettingsViewModel @AssistedInject constructor(
    settingsRepository: SettingsRepository,
    coroutineService: CoroutineService,
    private val settingsService: SettingsService,
    private val permissionService: PermissionService
) : ItemViewModel<Settings>(coroutineService, settingsRepository) {
    override var model = settingsRepository.get()

    var period = MutableLiveData<Period>()
    var startDayOfMonth = MutableLiveData<Int>()
    var startDayOfMonthVisible = MutableLiveData<Boolean>()
    var startDayOfWeek = MutableLiveData<Int>()
    var startDayOfWeekVisible = MutableLiveData<Boolean>()
    var captureSms = MutableLiveData<Boolean>()
    var captureNotifications = MutableLiveData<Boolean>()
    val appVersion = BuildConfig.VERSION_NAME

    private var onRequestSMSPermissions: () -> Unit = {  }
    private var onRequestNotificationPermissions: () -> Unit = {  }
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
        initProperties()
    }

    fun isThisFirstLaunch() = settingsService.isFirstLaunch()

    private fun initProperties() {
        period.value = model.period
        period.observeForever {
            model.period = it
            switchDateFields(it)
        }

        startDayOfMonth.value = availableDaysOfMonth.indexOf(model.startDayOfMonth.value)
        startDayOfMonth.observeForever {
            model.startDayOfMonth = DayOfMonth(availableDaysOfMonth[it])
        }

        startDayOfWeek.value = dayOfWeekMap.withIndex().filter { it.value.first == model.startDayOfWeek }.map { it.index }.first()
        startDayOfWeek.observeForever {
            model.startDayOfWeek = dayOfWeekMap[it].first
        }

        captureSms.value = model.captureSms
        captureSms.observeForever {
            if (model.captureSms != it) {
                model.captureSms = it

                if (it && !permissionService.isSMSAllowed()) {
                    onRequestSMSPermissions()
                }
            }
        }

        captureNotifications.value = model.captureNotifications
        captureNotifications.observeForever {
            if (model.captureNotifications != it) {
                model.captureNotifications = it

                if (it && !permissionService.isNotificationsAllowed()) {
                    onRequestNotificationPermissions()
                }
            }
        }
    }

    fun setMonthPeriod() {
        period.value = Period.MONTH
    }

    fun setWeekPeriod() {
        period.value = Period.WEEK
    }

    fun set2Week2Period() {
        period.value = Period.TWO_WEEKS
    }

    fun setQuarterPeriod() {
        period.value = Period.QUARTER
    }

    fun setYearPeriod() {
        period.value = Period.YEAR
    }

    fun onRequestSMSPermissions(handler: () -> Unit) {
        onRequestSMSPermissions = handler
    }

    fun onRequestNotificationPermissions(handler: () -> Unit) {
        onRequestNotificationPermissions = handler
    }

    private fun switchDateFields(period: Period) {
        startDayOfMonthVisible.value = false
        startDayOfWeekVisible.value = false

        when (period) {
            Period.MONTH -> startDayOfMonthVisible.value = true
            Period.WEEK, Period.TWO_WEEKS -> startDayOfWeekVisible.value = true
            else -> {}
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(): SettingsViewModel
    }
}
