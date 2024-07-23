package com.mas.mobile.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.mas.mobile.BuildConfig
import com.mas.mobile.domain.settings.DayOfMonth
import com.mas.mobile.domain.settings.Period
import com.mas.mobile.domain.settings.Settings
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.service.CoroutineService
import com.mas.mobile.service.PermissionService
import com.mas.mobile.service.ResourceService
import com.mas.mobile.util.DateTool
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.util.Currency

class SettingsViewModel @AssistedInject constructor(
    settingsRepository: SettingsRepository,
    coroutineService: CoroutineService,
    resourceService: ResourceService,
    private val permissionService: PermissionService
) : ItemViewModel<Settings>(coroutineService, settingsRepository) {
    override var model = settingsRepository.get()

    var period = MutableLiveData<String>()
    var startDayOfMonth = MutableLiveData<String>()
    var startDayOfMonthVisible = MutableLiveData<Boolean>()
    var startDayOfWeek = MutableLiveData<String>()
    var startDayOfWeekVisible = MutableLiveData<Boolean>()
    var currency = MutableLiveData<Currency>()
    var captureSms = MutableLiveData<Boolean>()
    var captureNotifications = MutableLiveData<Boolean>()
    var discoveryMode = MutableLiveData<Boolean>()
    val appVersion = BuildConfig.VERSION_NAME

    private var onRequestSMSPermissions: () -> Unit = {  }
    private var onRequestNotificationPermissions: () -> Unit = {  }
    private val dayOfWeekMap = mapOf(
        DayOfWeek.MONDAY to DateTool.formatToDayOfWeek(DayOfWeek.MONDAY),
        DayOfWeek.TUESDAY to DateTool.formatToDayOfWeek(DayOfWeek.TUESDAY),
        DayOfWeek.WEDNESDAY to DateTool.formatToDayOfWeek(DayOfWeek.WEDNESDAY),
        DayOfWeek.THURSDAY to DateTool.formatToDayOfWeek(DayOfWeek.THURSDAY),
        DayOfWeek.FRIDAY to DateTool.formatToDayOfWeek(DayOfWeek.FRIDAY),
        DayOfWeek.SATURDAY to DateTool.formatToDayOfWeek(DayOfWeek.SATURDAY),
        DayOfWeek.SUNDAY to DateTool.formatToDayOfWeek(DayOfWeek.SUNDAY)
    )
    val periodMap = mapOf(
        Period.WEEK to resourceService.constantPeriodWeek(),
        Period.TWO_WEEKS to resourceService.constantPeriodTwoWeeks(),
        Period.MONTH to resourceService.constantPeriodMonths(),
        Period.QUARTER to resourceService.constantPeriodQuarter(),
        Period.YEAR to resourceService.constantPeriodYear()
    )

    val availableDaysOfMonth = (1..28).map { it.toString() }.toList()
    val availableDaysOfWeek = dayOfWeekMap.entries.map { it.value }

    init {
        initProperties()
    }

    private fun initProperties() {
        period.value = periodMap[model.period]
        period.observeForever { p ->
            model.period = periodMap.entries.first { it.value == p }.key
            switchDateFields(model.period)
            save()
        }

        currency.value = model.currency
        currency.observeForever {
            model.currency = it
            save()
        }

        startDayOfMonth.value = model.startDayOfMonth.value.toString()
        startDayOfMonth.observeForever {
            model.startDayOfMonth = DayOfMonth(it.toInt())
            save()
        }

        startDayOfWeek.value = dayOfWeekMap[model.startDayOfWeek]
        startDayOfWeek.observeForever { d ->
            model.startDayOfWeek = dayOfWeekMap.entries.first { it.value == d }.key
            save()
        }

        captureSms.value = model.captureSms
        captureSms.observeForever {
            model.captureSms = permissionService.isSMSAllowed()
            if (it && !model.captureSms) {
                onRequestSMSPermissions()
            }
            save()
        }

        captureNotifications.value = model.captureNotifications
        captureNotifications.observeForever {
            model.captureNotifications = permissionService.isNotificationsAllowed()
            if (it && !model.captureNotifications) {
                onRequestNotificationPermissions()
            }
            save()
        }

        discoveryMode.value = model.discoveryMode
        discoveryMode.observeForever {
            model.discoveryMode = it
            save()
        }
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
