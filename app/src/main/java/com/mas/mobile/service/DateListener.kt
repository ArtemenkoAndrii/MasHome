package com.mas.mobile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mas.mobile.appComponent
import java.time.LocalDate
import javax.inject.Inject

class DateListener : BroadcastReceiver() {
    private var lastReloadDate = LocalDate.now()
    @Inject
    lateinit var budgetService: BudgetService


    override fun onReceive(context: Context, intent: Intent?) {
        context.appComponent.injectDateListener(this)
        if (lastReloadDate < LocalDate.now()) {
            budgetService.reloadBudget()
        }
        lastReloadDate = LocalDate.now()
    }
}