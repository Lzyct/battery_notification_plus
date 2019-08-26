package com.tuxdev.batterynotifplus.ui.battery

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.tuxdev.batterynotifplus.BuildConfig
import com.tuxdev.batterynotifplus.R
import com.tuxdev.batterynotifplus.base.BaseActivity
import com.tuxdev.batterynotifplus.data.DataBattery
import com.tuxdev.batterynotifplus.databinding.ActivityBatteryBinding
import com.tuxdev.batterynotifplus.services.BatteryReceiver
import com.tuxdev.batterynotifplus.ui.settings.SettingsActivity
import com.tuxdev.batterynotifplus.utils.Ping
import io.reactivex.functions.Consumer
import java.text.SimpleDateFormat
import java.util.*


class BatteryActivity : BaseActivity<ActivityBatteryBinding>() {

    override fun getToolbarResource(): Int = 0
    override fun getLayoutResource(): Int = R.layout.activity_battery

    private lateinit var batteryReceiver: BatteryReceiver
    private lateinit var mInterstitialAd: InterstitialAd

    override fun myCodeHere() {

        mInterstitialAd = InterstitialAd(this)
        if (BuildConfig.DEBUG)
            mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        else
            mInterstitialAd.adUnitId = "ca-app-pub-2962932702636730/1894764318"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        //setup wave view
        dataBinding.batteryWave.borderWidth = 15F
        dataBinding.batteryWave.centerTitleColor = ContextCompat.getColor(this, R.color.white)

        //get broadcast from listener
        Ping.listen(DataBattery::class.java, Consumer {
            dataBinding.dataBattery = it

            //set battery level
            dataBinding.batteryWave.progressValue = it.batteryPct?.toInt() ?: 0
            dataBinding.batteryWave.centerTitle = String.format(getString(R.string.battery_info_percent), it.batteryPct)

            dataBinding.tvStatus.text = it.status
            //wave speed
            if (it.status == getString(R.string.battery_info_status_charging)) {
                dataBinding.tvStatus.text = "${it.status} (${it.chargingSource})"
                if (it.chargingSource == getString(R.string.battery_info_power_usb))
                    dataBinding.batteryWave.setAnimDuration(2000L)
                else dataBinding.batteryWave.setAnimDuration(1000L)
            } else dataBinding.batteryWave.setAnimDuration(5000L)

            //change battery color by level
            if (it.batteryPct?.toInt() ?: 0 > 20) {
                dataBinding.tvStatus.setShadowLayer(5F, 3F, 3F, ContextCompat.getColor(this, R.color.green))
                dataBinding.batteryWave.waveColor = ContextCompat.getColor(this, R.color.green)
                dataBinding.batteryWave.setCenterTitleStrokeColor(ContextCompat.getColor(this, R.color.green), 10F)
            } else if (it.batteryPct?.toInt() ?: 0 < 20) {
                dataBinding.tvStatus.setShadowLayer(5F, 3F, 3F, ContextCompat.getColor(this, R.color.redSoft))
                dataBinding.batteryWave.waveColor = ContextCompat.getColor(this, R.color.redSoft)
                dataBinding.batteryWave.setCenterTitleStrokeColor(ContextCompat.getColor(this, R.color.redSoft), 10F)
            }
        }, Consumer
        {
            it.printStackTrace()
        })

        dataBinding.ivHeader.setOnClickListener {
            startActivity(Intent(applicationContext, SettingsActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }

    override fun onResume() {
        super.onResume()
        //set theme by time
        val sdf = SimpleDateFormat("HH")
        val currentClock = sdf.format(Date())
        if (currentClock.toInt() >= 18 || currentClock.toInt() <= 6) {
            dataBinding.ivHeader.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.header_night))
            theme.applyStyle(R.style.AppThemeNight, true)
        } else {
            dataBinding.ivHeader.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.header_morning))
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            theme.applyStyle(R.style.AppTheme, true)
        }
        batteryReceiver = BatteryReceiver(true)
        registerReceiver(batteryReceiver, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))
        mInterstitialAd.show()
    }
}
