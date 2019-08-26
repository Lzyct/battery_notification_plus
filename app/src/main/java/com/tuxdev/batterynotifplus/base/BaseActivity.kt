package com.tuxdev.batterynotifplus.base

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.tuxdev.batterynotifplus.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * *********************************************
 * Created by ukie on 9/26/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 * *********************************************
 * © 2018 | All Right Reserved
 */
abstract class BaseActivity<B : ViewDataBinding> : AppCompatActivity() {
    protected var toolbar: Toolbar? = null
    protected lateinit var dataBinding: B

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        //set theme by time
        val sdf = SimpleDateFormat("HH")
        val currentClock = sdf.format(Date())
        if (currentClock.toInt() >= 18 || currentClock.toInt() <= 6) {
            theme.applyStyle(R.style.AppThemeNight, true)
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            theme.applyStyle(R.style.AppTheme, true)
        }

        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, getLayoutResource())
        if (getToolbarResource() != 0) {
            toolbar = findViewById(getToolbarResource())
            setSupportActionBar(toolbar)
            supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        myCodeHere()
    }

    protected abstract fun getToolbarResource(): Int
    protected abstract fun getLayoutResource(): Int
    protected abstract fun myCodeHere()


    override fun onResume() {
        super.onResume()
        val sdf = SimpleDateFormat("HH")
        val currentClock = sdf.format(Date())
        if (currentClock.toInt() >= 18 || currentClock.toInt() <= 6) {
            theme.applyStyle(R.style.AppThemeNight, true)
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            theme.applyStyle(R.style.AppTheme, true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> supportFinishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }
}