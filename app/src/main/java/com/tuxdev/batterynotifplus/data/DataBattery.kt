package com.tuxdev.batterynotifplus.data

/**
 **********************************************
 * Created by ukie on 11/17/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 **********************************************
 * © 2018 | All Right Reserved
 */
data class DataBattery(
        val status: String? = "",
        val chargingSource: String? = "",
        val batteryPct: String? = "0%",
        val batteryTmp: String? = "",
        val batteryVolt: String? = "",
        val designCapacity: String? = "",
        val currentCapacity: String? = "",
        val batteryHealth: String? = "",
        val timeBoot: String? = "",
        val technology: String? = "",
        val health: String? = "",
        val fastCharge: String? = "",
        val cycleCount: String? = ""
)