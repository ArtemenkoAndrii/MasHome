package com.mas.mobile.presentation.activity.converter

import android.util.Log
import androidx.databinding.InverseMethod
import com.mas.mobile.util.DateTool
import java.time.LocalDate
import java.time.LocalDateTime

object DateConverter {
    @InverseMethod("stringToDate")
    @JvmStatic
    fun dateToString(value: LocalDate?) =
        value?.let { DateTool.dateToString(it) }

    @JvmStatic
    fun stringToDate(value: String?) =
        value?.let { DateTool.stringToDate(it) }

    @InverseMethod("stringToDateTime")
    @JvmStatic
    fun dateTimeToString(value: LocalDateTime?): String {
        return if (value == null) {
            ""
        } else {
            DateTool.dateTimeToString(value)
        }
    }

    @JvmStatic
    fun stringToDateTime(value: String): LocalDateTime {
        return DateTool.stringToDateTime(value)
    }

    @InverseMethod("relativeDateAsStringToDateTime")
    @JvmStatic
    fun dateTimeToRelativeDateAsString(value: LocalDateTime): String {
        return DateTool.relativeDate(value)
    }

    @JvmStatic
    fun relativeDateAsStringToDateTime(value: String): LocalDateTime {
        Log.e(this::class.simpleName, "relativeDateAsStringToDateTime is not implemented!")
        return LocalDateTime.now()
    }

    @InverseMethod("timeAsStringToDateTime")
    @JvmStatic
    fun dateTimeToTimeAsString(value: LocalDateTime): String {
        return DateTool.timeAsString(value)
    }

    @JvmStatic
    fun timeAsStringToDateTime(value: String): LocalDateTime {
        Log.e(this::class.simpleName, "timeAsStringToDateTime is not implemented!")
        return LocalDateTime.now()
    }
}