package com.mas.mobile.presentation.listener

import android.view.MenuItem
import android.view.View
import com.mas.mobile.R

interface ListListener<T> {
    fun onRowItemClick(view: View, rowItem: T)
    fun onRowMenuClick(item: MenuItem, rowItem: T): Boolean
    fun showRowMenu(): Int = R.menu.standard_row_menu
}