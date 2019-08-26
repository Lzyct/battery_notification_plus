@file:Suppress("DEPRECATION")

package com.tuxdev.batterynotifplus.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdView
import com.tuxdev.batterynotifplus.BuildConfig
import com.tuxdev.batterynotifplus.R
import com.tuxdev.batterynotifplus.base.BaseActivity
import com.tuxdev.batterynotifplus.databinding.ActivitySettingsBinding
import com.tuxdev.batterynotifplus.services.BNService


class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun getToolbarResource(): Int = R.id.toolbar

    override fun getLayoutResource(): Int = R.layout.activity_settings

    override fun myCodeHere() {
        val adView = PublisherAdView(this)
        adView.setAdSizes(AdSize.SMART_BANNER)
        if (BuildConfig.DEBUG)
            adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        else
            adView.adUnitId = "ca-app-pub-2962932702636730/6794039683"
        adView.setAdSizes(AdSize.SMART_BANNER)
        adView.loadAd(PublisherAdRequest.Builder().build())
        dataBinding.bannerView.addView(adView)

        supportActionBar?.title = getString(R.string.action_settings)

        fragmentManager.beginTransaction()
                // .replace(android.R.id.content, SettingsFragment())
                .replace(R.id.fl_container, SettingsFragment())
                .commit()

    }

    override fun onResume() {
        super.onResume()
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        @ColorInt val color = typedValue.data
        window.statusBarColor = color
    }

    class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_settings)
            setPref("low_sound")
            setPref("full_sound")
            setPref("charge_sound")
            setPref("discharge_sound")

            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(activity.applicationContext)

            val lowLevel = findPreference("value_battery_low") as ListPreference
            lowLevel.summary = "${prefs?.getString("value_battery_low", getString(R.string.set_value_summary))}%%"

            val tmpAlert = findPreference("tmp_alert") as EditTextPreference
            tmpAlert.summary = prefs.getString("tmp_alert", "")

            val email = findPreference("email")
            email.setOnPreferenceClickListener {
                val mailIntent = Intent(Intent.ACTION_SENDTO)
                mailIntent.data = Uri.parse("mailto:ukie.tux@gmail.com")
                activity.startActivity(mailIntent)
                return@setOnPreferenceClickListener true
            }

            val changelog = findPreference("changelog") as PreferenceScreen
            changelog.setOnPreferenceClickListener {
                AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.changelog_title))
                        .setMessage(getString(R.string.changelog))
                        .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                return@setOnPreferenceClickListener true
            }

            val runService = findPreference("key_background") as CheckBoxPreference
            runService.setOnPreferenceClickListener {
                val service = Intent(activity, BNService::class.java)
                if (runService.isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity?.startForegroundService(service)
                    } else activity?.startService(service)
                } else {
                    activity.stopService(service)
                }
                return@setOnPreferenceClickListener true

            }
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            setPref("low_sound")
            setPref("full_sound")
            setPref("charge_sound")
            setPref("discharge_sound")
            setPref("tmp_sound")

            //update low summary
            val lowLevel = findPreference("value_battery_low") as ListPreference
            lowLevel.summary = "${sharedPreferences?.getString("value_battery_low", getString(R.string.set_value_summary))}%%"

            //update tmp alert summary
            val tmpAlert = findPreference("tmp_alert") as EditTextPreference
            tmpAlert.summary = sharedPreferences?.getString("tmp_alert", "")
        }

        private fun setPref(prefKey: String) {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(activity.applicationContext)

            val preference = prefs.getString(prefKey,
                    "DEFAULT_RINGTONE_URI")
            val prefUri = Uri.parse(preference)
            if (prefUri.toString() == "DEFAULT_RINGTONE_URI" || prefUri.toString().isEmpty()) {
                val pref = findPreference(prefKey) as RingtonePreference
                pref.summary = "Silent"
            } else {
                val prefRingtone = RingtoneManager.getRingtone(
                        activity.applicationContext, prefUri)
                val pref = findPreference(prefKey) as RingtonePreference
                pref.summary = prefRingtone.getTitle(activity.applicationContext)
            }
        }
    }
}
