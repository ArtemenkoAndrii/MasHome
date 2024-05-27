package com.mas.mobile.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.mas.mobile.appComponent
import com.mas.mobile.domain.settings.DeferrableActionService
import com.mas.mobile.domain.settings.ProgressiveDaily
import com.mas.mobile.util.Analytics
import com.mas.mobile.util.Analytics.Event.APP_UPDATE_SUGGESTED
import javax.inject.Inject

class AppUpdateCheckWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    @Inject
    lateinit var deferrableActionService: DeferrableActionService
    @Inject
    lateinit var notificationService: NotificationService
    @Inject
    lateinit var analytics: Analytics

    override fun doWork(): Result {
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val action = ProgressiveDaily("Version available: ${appUpdateInfo.availableVersionCode()}")
                if (!deferrableActionService.isDeferred(action)) {
                    deferrableActionService.defer(action)
                    notificationService.sendUpdateAvailable()
                    analytics.logEvent(APP_UPDATE_SUGGESTED)
                }
            }
        }.addOnFailureListener {
            Log.e(this::class.java.name, it.message, it)
        }

        return Result.success()
    }

    init {
        context.appComponent.injectAppUpdateCheckWorker(this)
    }
}
