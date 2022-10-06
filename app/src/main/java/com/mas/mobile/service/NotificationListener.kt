package com.mas.mobile.service

import android.app.Notification
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.content.pm.PackageManager
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

            val message = if (title == null && text == null) {
                grabCustomContentView(sbn.notification)
            } else {
                "$title\n$text"
            }

            process(Message(sender, postDate, message))
        }
    }

    private fun grabCustomContentView(notification: Notification) =
        try {
            val actions = getPrivateField<List<*>>("mActions", notification.contentView)
            actions?.filterNotNull()
                ?.filter { getPrivateField<String>("methodName", it) == "setText" }
                ?.map { getPrivateField<String>("value", it) }
                ?.joinToString("\n")
        } catch (e: Throwable) {
            Log.e(this::class.simpleName, "Can't grab notification from custom View", e)
            null
        } ?: ""

    private fun <T> getPrivateField(name: String, instance: Any): T?  {
        var clazz: Class<Any>? = instance.javaClass
        do {
            if (clazz!!.declaredFields.any { it.name == name }) {
                break
            }
            clazz = clazz.superclass
        } while (clazz != null)

        return clazz?.getDeclaredField(name)
            ?.also { it.isAccessible = true }
            ?.get(instance) as T
    }

    private fun extractSender(sbn: StatusBarNotification) =
        this.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .firstOrNull { it.packageName == sbn.packageName }?.let {
                this.packageManager.getApplicationLabel(it)
            }

    private fun toLocalDateTime(time: Long): LocalDateTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            TimeZone.getDefault().toZoneId())
}