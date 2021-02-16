package com.mas.mobile.repository.db.config.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

const val TRUE: Byte = 1
const val FALSE: Byte = 0

class SQLiteTypeConverter {

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()) }
    }

    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return toLocalDateTime(value)?.toLocalDate()
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return fromLocalDateTime(date?.atStartOfDay())
    }

    @TypeConverter
    fun toBoolean(value: Byte?): Boolean? {
        return value?.equals(TRUE)
    }

    @TypeConverter
    fun fromBoolean(value: Boolean?): Byte? {
        return value?.let{ if (it) TRUE else FALSE }
    }
}