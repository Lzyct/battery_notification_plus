package com.tuxdev.batterynotifplus.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log

/**
 **********************************************
 * Created by ukie on 1/9/19 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 **********************************************
 * © 2019 | All Right Reserved
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val runBackground = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_background", false)
        if (runBackground) {
            val service = Intent(context, BNService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(service)
            } else context?.startService(service)
        } else {
            context?.registerReceiver(BatteryReceiver(false), IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED))
        }
    }

}