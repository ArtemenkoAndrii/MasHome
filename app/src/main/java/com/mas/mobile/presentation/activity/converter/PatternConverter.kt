package com.mas.mobile.presentation.activity.converter

import androidx.databinding.InverseMethod
import com.mas.mobile.domain.message.Pattern

object PatternConverter {
    @InverseMethod("stringToPattern")
    @JvmStatic
    fun patternToString(value: String): Pattern {
        return Pattern(value)
    }

    @JvmStatic
    fun stringToPattern(value: Pattern): String {
        return value.value
    }
}