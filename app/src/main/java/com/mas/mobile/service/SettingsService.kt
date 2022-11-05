package com.mas.mobile.service

import android.content.Context
import android.util.Log
import com.mas.mobile.BuildConfig
import com.mas.mobile.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsService @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val coroutineService: CoroutineService
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

    fun needToShowPolicy() =
        settingsRepository.get().policyVersion != POLICY_VERSION

    fun confirmPolicyReading() {
        coroutineService.backgroundTask {
            val settings = settingsRepository.get()
            settings.policyVersion = POLICY_VERSION
            settingsRepository.update(settings)
        }
    }

    companion object {
        private const val FIRST_RUN_KEY = "firstRun"
        const val POLICY_VERSION = "privacy_policy_v1"
    }
}
