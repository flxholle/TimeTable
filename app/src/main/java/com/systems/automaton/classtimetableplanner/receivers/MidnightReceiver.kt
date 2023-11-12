package com.systems.automaton.classtimetableplanner.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.systems.automaton.classtimetableplanner.utils.PreferenceUtil

class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null) {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
                PreferenceUtil.setOneTimeAlarm(context, MidnightReceiver::class.java, hour, minutes, 0, MidnightRecieverID)
                return
            }
        }

        val bootIntent = Intent()
        bootIntent.setAction(Intent.ACTION_BOOT_COMPLETED)
        val dailyReceiver = DailyReceiver()
        dailyReceiver.onReceive(context, bootIntent)
        val turnOnReceiver = TurnOnReceiver()
        turnOnReceiver.onReceive(context, bootIntent)
        PreferenceUtil.setOneTimeAlarm(context, MidnightReceiver::class.java, hour, minutes, 0, MidnightRecieverID)
    }

    companion object {
        val hour = 0
        val minutes = 15
        val MidnightRecieverID = 123155
    }
}