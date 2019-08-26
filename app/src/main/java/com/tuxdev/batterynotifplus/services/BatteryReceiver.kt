package com.tuxdev.batterynotifplus.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.BatteryManager
import android.os.SystemClock
import android.preference.PreferenceManager
import android.text.format.DateUtils
import android.util.Log
import com.tuxdev.batterynotifplus.R
import com.tuxdev.batterynotifplus.data.DataBattery
import com.tuxdev.batterynotifplus.utils.Ping
import java.io.File
import java.io.FileNotFoundException
import java.text.DecimalFormat


/**
 **********************************************
 * Created by ukie on 9/22/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 **********************************************
 * © 2018 | All Right Reserved
 */
class BatteryReceiver(private var isActivity: Boolean? = false) : BroadcastReceiver() {

    /**
     * that variable using to fix double sound
     * when battery status changed
     */
    private var wasChargeSound = false
    private var wasDischargeSound = false
    private var wasLowSound = false
    private var wasFullSound = false


    @SuppressLint("PrivateApi")
    override fun onReceive(context: Context?, intent: Intent?) {
        val status: Int? = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        var statusString = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> context?.getString(R.string.battery_info_status_charging)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> context?.getString(R.string.battery_info_status_discharging)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context?.getString(R.string.battery_info_status_not_charging)
            BatteryManager.BATTERY_STATUS_FULL -> context?.getString(R.string.battery_info_status_full)
            else -> context?.getString(R.string.battery_info_status_unknown)
        }

        //get charging source
        val plugType = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val chargingSource: String = when (plugType) {
            0 -> context?.getString(R.string.battery_info_power_unplugged) ?: ""
            BatteryManager.BATTERY_PLUGGED_AC -> context?.getString(R.string.battery_info_power_ac)
                    ?: ""
            BatteryManager.BATTERY_PLUGGED_USB -> context?.getString(R.string.battery_info_power_usb)
                    ?: ""
            BatteryManager.BATTERY_PLUGGED_AC or BatteryManager.BATTERY_PLUGGED_USB -> context?.getString(R.string.battery_info_power_ac_usb)
                    ?: ""
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context?.getString(R.string.battery_info_power_wireless)
                    ?: ""
            else -> context?.getString(R.string.battery_info_power_unknown) ?: ""
        }

        val tmp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) // get temperature
        val volt = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) // get voltage
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) // get battery type
        val uptime = DateUtils.formatElapsedTime(SystemClock.elapsedRealtime() / 1000) // count uptime

        val myFile = File("/sys/kernel/fast_charge/force_fast_charge")
        var fastCharge = false
        if (myFile.exists())
            fastCharge = true

        //get batteryPercent
        val batteryPct: Float? = intent?.let { i ->
            val level: Int = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level / scale.toFloat()
        }

        // get health
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val healthString: String = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> context?.getString(R.string.battery_info_health_good)
                    ?: ""
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context?.getString(R.string.battery_info_health_overheat)
                    ?: ""
            BatteryManager.BATTERY_HEALTH_DEAD -> context?.getString(R.string.battery_info_health_dead)
                    ?: ""
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context?.getString(R.string.battery_info_health_over_voltage)
                    ?: ""
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context?.getString(R.string.battery_info_health_unspecified_failure)
                    ?: ""
            else -> context?.getString(R.string.battery_info_health_unknown) ?: ""
        }

        //get current capacity
        val mBatteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        var currentCapacity = 0L
        if (chargeCounter != null && capacity != null) {
            currentCapacity = (chargeCounter / capacity) / 10
        }

        //get battery design capacity
        val mPowerProfile: Any
        var designCapacity = 0.0
        val classPowerProfile = "com.android.internal.os.PowerProfile"

        try {
            mPowerProfile = Class.forName(classPowerProfile)
                    .getConstructor(Context::class.java)
                    .newInstance(context)

            designCapacity = Class
                    .forName(classPowerProfile)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile) as Double

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cycleCount = try {
            File("/sys/class/power_supply/battery/battery_cycle").readText(Charsets.UTF_8)
        } catch (e: FileNotFoundException) {
            "-"
        }

//        try {
//            val test = Runtime.getRuntime().exec("cat /sys/class/power_supply/battery/battery_cycle")
//            Log.e("BNService", test.toString())
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        }
        val round = DecimalFormat("##.##")
        val check = isActivity ?: throw NullPointerException()
        if (!check) {
            //set ringtone
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> {
                    if (!wasChargeSound) {
                        playSound(context, "charge_sound")
                        wasChargeSound = true
                        wasDischargeSound = false
                        wasLowSound = true
                        wasFullSound = false
                    }
                }
                BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                    if (!wasDischargeSound) {
                        playSound(context, "discharge_sound")
                        wasDischargeSound = true
                        wasChargeSound = false
                        wasLowSound = false
                        wasFullSound = false
                    }
                }
                BatteryManager.BATTERY_STATUS_FULL -> {
                    if (!wasFullSound) {
                        playSound(context, "full_sound")
                        wasFullSound = true
                    }
                }
            }

            val lowLevel = PreferenceManager.getDefaultSharedPreferences(context).getString("value_battery_low", "0")
            if (batteryPct?.times(100)?.toInt()!! <= lowLevel.toInt()) {
                if (!wasLowSound) {
                    playSound(context, "low_sound")
                    wasLowSound = true
                }
                if (statusString != context.getString(R.string.battery_info_status_charging))
                    statusString = context.getString(R.string.battery_info_status_low)
            } else {
                if (!wasChargeSound)
                    wasLowSound = true
            }

            //temperature alert
            val tempAlert = PreferenceManager.getDefaultSharedPreferences(context).getString("tmp_alert", "0")
            if (tenthsToFixedString(tmp ?: 0).toFloat() >= tempAlert.toInt()) {
                playSound(context, "tmp_sound")
            }
        }

        val dataBattery = DataBattery(
                status = statusString,
                chargingSource = chargingSource,
                batteryTmp = tenthsToFixedString(tmp ?: 0),
                batteryVolt = "$volt", // mV
                technology = technology,
                batteryPct = "${batteryPct?.times(100)?.toInt()}", // %
                designCapacity = "${designCapacity.toInt()}", // mAh
                currentCapacity = if (currentCapacity < 0) "-" else "$currentCapacity",// mAh
                batteryHealth = round.format((currentCapacity / designCapacity) * 100),
                timeBoot = uptime,
                health = healthString,
                fastCharge = fastCharge.toString(),
                cycleCount = cycleCount)
        //broadcast data
        Ping.broadcast(dataBattery)
    }

    private fun playSound(context: Context, prefKey: String) {

        val ringtonePreference: String?
        val prefs = PreferenceManager
                .getDefaultSharedPreferences(context.applicationContext)
        ringtonePreference = prefs.getString(prefKey,
                "")
        val prefUri = Uri.parse(ringtonePreference)
        if (prefUri.toString().isNotEmpty() || prefUri.toString().isNotBlank()) {
            val ringtone = RingtoneManager.getRingtone(
                    context.applicationContext, prefUri)
            ringtone.play()
        }
    }

    private fun tenthsToFixedString(x: Int): String {
        val tens = x / 10
        return Integer.toString(tens) + "." + (x - 10 * tens)
    }
}