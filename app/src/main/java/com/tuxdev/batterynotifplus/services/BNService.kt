package com.tuxdev.batterynotifplus.services

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tuxdev.batterynotifplus.R
import com.tuxdev.batterynotifplus.data.DataBattery
import com.tuxdev.batterynotifplus.ui.battery.BatteryActivity
import com.tuxdev.batterynotifplus.utils.Ping
import io.reactivex.functions.Consumer

/**
 **********************************************
 * Created by ukie on 1/10/19 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 **********************************************
 * © 2019 | All Right Reserved
 */
class BNService : Service() {

    private lateinit var batteryReceiver: BatteryReceiver
    override fun onCreate() {
        super.onCreate()
        batteryReceiver = BatteryReceiver(false)
        registerReceiver(batteryReceiver, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))

        Ping.listen(DataBattery::class.java,
                Consumer {
                    if (it.status == getString(R.string.battery_info_status_full) || it.status == getString(R.string.battery_info_status_low))
                        alert("${it.status} , Battery Level : ${it.batteryPct} % , Tmp : ${it.batteryTmp} °C ")
                    notification(it.status
                            ?: "", " Battery Level : ${it.batteryPct} % , Tmp : ${it.batteryTmp} °C")
                }, Consumer
        {
            it.printStackTrace()
        })
    }

    private fun alert(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun notification(title: String, message: String) {
        val notifIntent: Intent?
        notifIntent = Intent(applicationContext, BatteryActivity::class.java)
        notifIntent.putExtra("notifIntent", true)
        notifIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)


        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder: NotificationCompat.Builder
        val inboxStyle = NotificationCompat.InboxStyle()

        @TargetApi(Build.VERSION_CODES.O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */

            val mChannel = NotificationChannel(getString(R.string.app_name),
                    title,
                    NotificationManager.IMPORTANCE_DEFAULT)

            mChannel.enableLights(true)
            mChannel.lightColor = Color.GREEN
            mChannel.setShowBadge(true)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(mChannel)

            val pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            inboxStyle.addLine(message)

            notificationBuilder = NotificationCompat.Builder(applicationContext, getString(R.string.app_name))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setChannelId(getString(R.string.app_name))
                    .setColor(ContextCompat.getColor(this, R.color.night))
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setTicker(message)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())

        } else {
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            inboxStyle.addLine(message)

            @Suppress("DEPRECATION")
            notificationBuilder = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setTicker(title)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setColor(ContextCompat.getColor(this, R.color.night))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setStyle(inboxStyle)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setWhen(System.currentTimeMillis())
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setContentText(message)
        }
        startForeground(1, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}