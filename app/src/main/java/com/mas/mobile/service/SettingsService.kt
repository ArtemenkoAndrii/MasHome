package com.mas.mobile.service

import android.content.Context
import android.util.Log
import com.mas.mobile.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsService @Inject constructor(
    private val context: Context
) {
    fun isThisFirstLaunch(): Boolean =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, 0).getBoolean(FIRST_RUN_KEY, true)

    fun commitFirstRun() {
        if (isThisFirstLaunch()) {
            Log.i(this::class.simpleName, "Committing first app run.")
            context.getSharedPreferences(BuildConfig.APPLICATION_ID, 0).edit().also {
                it.putBoolean(FIRST_RUN_KEY, false)
                it.commit()
            }
        }
    }

    private companion object {
        const val FIRST_RUN_KEY = "firstRun"
    }
}
