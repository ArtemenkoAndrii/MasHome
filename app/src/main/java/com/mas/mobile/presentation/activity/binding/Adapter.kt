package com.mas.mobile.presentation.activity.binding

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.mas.mobile.presentation.activity.converter.MoneyConverter

// Populates text input on model changed
@BindingAdapter("android:text")
fun TextInputEditText.bindEditText(value: Double) {
    val editorValue = this.text.toString()
    if (this.hasFocus()) {
        // Forbid more then two numbers after the dot
        val split = editorValue.split(".")
        if (split.size > 1 && split[1].length > 2) {
            val new = editorValue.substring(0, split[0].length+3)
            this.setText(new)
            this.setSelection(new.length)
        }
    } else {
        // Populate initial value
        val modelValue = MoneyConverter.doubleToString(value)
        if (editorValue != modelValue) {
            this.setText(modelValue)
        }
    }
}

// Populates model on text changed
@InverseBindingAdapter(attribute = "android:text")
fun TextInputEditText.bindInverseEditText(): Double {
    val textValue = this.text.toString()
    if (textValue.isEmpty()) return 0.0

    return MoneyConverter.stringToDouble(this.text.toString())
}

// Populates input on focus changed
@BindingAdapter("app:onFocusLost")
fun TextInputEditText.onFocusLost(value: Double) {
    setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            // Format the input if the focus is lost
            this.setText(MoneyConverter.doubleToString(value))
        } else {
            // Clear the input for the new value
            if (value == 0.0) { this.setText("") }
        }
    }
}