package com.mas.mobile.service

import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.mas.mobile.appComponent
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class NotificationListener: NotificationListenerService() {
    @Inject
    lateinit var messageService: SpendingMessageService

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        this.appComponent.injectNotificationListener(this)

        ifMessage(sbn) {
            messageService.processMessage(it)
        }
    }

    private fun ifMessage(sbn: StatusBarNotification?, process: (message: Message) -> Unit) {
        if (sbn != null) {
            val postDate = toLocalDateTime(sbn.postTime)
            val sender = extractSender(sbn).toString()

            val title = sbn.notification.extras[EXTRA_TITLE]?.toString()
            val text = sbn.notification.extras[EXTRA_TEXT]?.toString()
            val message = "$title\n$text"

            if (postDate!= null && sender!= null && message != null) {
                process(Message(sender, postDate, message))
            }
        }
    }

    private fun extractSender(sbn: StatusBarNotification) =
        this.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .firstOrNull { it.packageName == sbn.packageName }?.let {
                this.packageManager.getApplicationLabel(it)
            }

    private fun toLocalDateTime(time: Long): LocalDateTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            TimeZone.getDefault().toZoneId());

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("WWWW", "onBind")
        return super.onBind(intent)
    }

    init {
        Log.d("WWWW", "init")
        val w1 = null
    }
}