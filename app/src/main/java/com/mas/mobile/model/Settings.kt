package com.mas.mobile.model

import java.time.DayOfWeek
import java.time.LocalDate

data class Settings (
    var period: Period = Period.MONTH,
    var startDayOfMonth: DayOfMonth = DayOfMonth(LocalDate.now().dayOfMonth),
    var startDayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
    var captureSms: Boolean = false,
    var captureNotifications: Boolean = false
)

enum class Period {
    MONTH, WEEK, TWO_WEEKS, QUARTER, YEAR
}

@JvmInline
value class DayOfMonth(val value: Int) {
    init {
        require(value in 1..31)
    }
}
