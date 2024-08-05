package com.mas.mobile.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class Analytics {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(eventName: EventName, param: ParamName, value: String) {
        logEvent(eventName) {
            it.putString(param.value, value)
        }
    }

    fun logEvent(eventName: EventName, params: (Bundle) -> Unit = {}) {
        val bundle = Bundle()
        params(bundle)
        firebaseAnalytics.logEvent(eventName.value, bundle)
    }

    object Event {
        val SPENDING_CREATED = EventName("spending_created")
        val MESSAGE_EVALUATED = EventName("message_evaluated")
        val MESSAGE_TEMPLATE_FAILED = EventName("message_template_failed")
        val APP_UPDATE_SUGGESTED = EventName("app_update_suggested")
    }

    object Param {
        val SOURCE = ParamName("source")
        val STATUS = ParamName("status")
    }

    @JvmInline
    value class EventName(val value: String)

    @JvmInline
    value class ParamName(val value: String)
}