package com.tuxdev.batterynotifplus.base

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.tuxdev.batterynotifplus.di.batteryNotifApp
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin


/**
 * *********************************************
 * Created by ukie on 9/26/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 * *********************************************
 * © 2018 | All Right Reserved
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        val formatStrategy = PrettyFormatStrategy.newBuilder()
//                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
//                .methodCount(2)         // (Optional) How many method line to show. Default 2
//                .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
//                .tag("BNPlus")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
//                .build()
//        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        //enable crashlytics in debugging
        val fabric = Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(false)           // Enables Crashlytics debugger
                .build()
        Fabric.with(fabric)

        //register ads
        MobileAds.initialize(this, "ca-app-pub-2962932702636730~3549027308")

//        val mBatteryReceiver = BatteryReceiver(false)
//        val batteryLevelFilter = IntentFilter(
//                Intent.ACTION_BATTERY_CHANGED)
//        registerReceiver(mBatteryReceiver, batteryLevelFilter)

        //Insert Koin
        startKoin(this, batteryNotifApp)
    }

}
