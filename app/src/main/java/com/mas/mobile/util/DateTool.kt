package com.mas.mobile.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class DateTool {
    companion object {
        private const val DAY_MONTH : String = "dd MM"
        private const val TIME : String = "HH:mm"
        private const val DATE_TIME = "d.MM HH:mm"
        private const val DATE = "dd/MM/yyyy"
        private const val TODAY = "Today"
        private const val YESTERDAY = "Yesterday"

        fun relativeDate(date: LocalDateTime): String {
            val localDate = date.toLocalDate()
            return when {
                localDate.equals(LocalDate.now()) -> TODAY
                localDate.equals(LocalDate.now().minusDays(1)) -> YESTERDAY
                else -> dayAsString(date)
            }
        }

        private fun dayAsString(date: LocalDateTime): String {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DAY_MONTH)
            return date.format(formatter)
        }

        fun timeAsString(date: LocalDateTime): String {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME)
            return date.format(formatter)
        }

        fun dateTimeToString(date: LocalDateTime): String {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME)
            return date.format(formatter)
        }

        fun stringToDateTime(date: String): LocalDateTime {
            val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
                .appendPattern(DATE_TIME)
                .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
                .toFormatter()
            return LocalDateTime.parse(date, formatter)
        }

        fun dateToString(date: LocalDate): String {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE)
            return date.format(formatter)
        }

        fun stringToDate(date: String): LocalDate {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE)
            return LocalDate.parse(date, formatter)
        }

    }
}