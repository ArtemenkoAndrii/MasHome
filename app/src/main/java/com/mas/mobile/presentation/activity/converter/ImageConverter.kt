package com.mas.mobile.presentation.activity.converter

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.InverseMethod
import com.mas.mobile.R

object ImageConverter {

    @InverseMethod("intToImg")
    @JvmStatic
    fun imgToInt(expenseId: Int?): Drawable {
        val img = if (expenseId != null) {
            R.drawable.link24
        } else {
            R.drawable.attention24
        }

        return ResourcesCompat.getDrawable(Resources.getSystem(), img, null)!!
    }

    @JvmStatic
    fun intToImg(expenseId: Int?): Int =
        if (expenseId != null) {
            R.drawable.link24
        } else {
            R.drawable.attention24
        }

}