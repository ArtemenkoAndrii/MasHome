package com.mas.mobile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.widget.Toast

const val SMS_BUNDLE = "pdus"
const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"

class SmsReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SMS_RECEIVED && intent.extras != null) {
            val intentExtras = intent.extras!!
            val sms = intentExtras.get(SMS_BUNDLE) as Array<Any>

            (sms.indices).forEach { i ->
                val format = intentExtras.getString("format")
                val smsMessage = SmsMessage.createFromPdu(
                    sms[i] as ByteArray,
                    format
                )

                val smsSender = smsMessage.originatingAddress.toString()
                val smsBody = smsMessage.messageBody.toString()

                Toast.makeText(context, smsSender + smsBody, Toast.LENGTH_SHORT).show()
            }
        }
    }

}