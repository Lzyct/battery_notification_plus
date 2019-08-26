package com.tuxdev.batterynotifplus.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * *********************************************
 * Created by ukie on 9/28/18 with ♥
 * (>’_’)> email : ukie.tux@gmail.com
 * github : https://www.github.com/tuxkids <(’_’<)
 * *********************************************
 * © 2018 | All Right Reserved
 */
class PrefManager
constructor(var context: Context) {
    private val PREF_LOGIN = "UserLogin"
    private val PREF_NAME = "SIPNASPIP"
    private val PREF_AUTH = "Auth"
    private val sharedPref: SharedPreferences

    init {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun clear() {
        sharedPref.edit().clear().apply()
    }

    fun saveString(key: String, value: String) {
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? = sharedPref.getString(key, "")

    fun saveInt(key: String, value: Int) {
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String): Int = sharedPref.getInt(key, 0)

    fun getBoolean(key: String): Boolean = sharedPref.getBoolean(key, false)

    fun saveBoolean(key: String, value: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }


    fun setUserLogin(boolean: Boolean) = saveBoolean(PREF_LOGIN, boolean)

    fun setAuthToken(token: String) = saveString(PREF_AUTH, "Bearer $token")

    fun getUserLogin(): Boolean = getBoolean(PREF_LOGIN)

    fun getAuthToken(): String? = getString(PREF_AUTH)


    fun userLogout() {
        clear()
    }
}
